package com.snapandpack.service;

import com.snapandpack.dto.AuthDtos.AuthResponse;
import com.snapandpack.dto.AuthDtos.LoginRequest;
import com.snapandpack.dto.AuthDtos.RegisterRequest;
import com.snapandpack.dto.UserDtos.UserResponse;
import com.snapandpack.exception.ApiException;
import com.snapandpack.model.Role;
import com.snapandpack.model.User;
import com.snapandpack.repository.UserRepository;
import com.snapandpack.security.TokenService;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Registering and signing in, including the lock that stops password guessing. */
@Service
public class AuthService {

    /** Wrong passwords allowed in a row before the account is locked. */
    static final int MAX_FAILED_LOGINS = 5;
    /** How long a locked account stays locked. */
    static final Duration LOCK_TIME = Duration.ofMinutes(15);

    private final UserRepository users;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokens;
    private final AuditService audit;
    /** Checked when the email is unknown, so a wrong email takes as long to answer as a wrong password. */
    private final String dummyHash;

    /** Receives the services this class needs and prepares the stand-in hash for unknown emails. */
    public AuthService(UserRepository users, UserService userService, PasswordEncoder passwordEncoder,
                       TokenService tokens, AuditService audit) {
        this.users = users;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.tokens = tokens;
        this.audit = audit;
        this.dummyHash = passwordEncoder.encode("not-a-real-password");
    }

    /** Creates a customer account and signs it in. Staff accounts can only be created by the admin. */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        User user = userService.createAccount(request.name(), request.email(), request.password(), Role.CUSTOMER);
        return new AuthResponse(tokens.issue(user), UserResponse.from(user));
    }

    /**
     * Checks the email and password and returns a sign-in token.
     * After five wrong passwords in a row the account is locked for 15 minutes.
     * The failed-attempt count is saved even though the request ends in an error.
     */
    @Transactional(noRollbackFor = ApiException.class)
    public AuthResponse login(LoginRequest request) {
        User user = users.findByEmailIgnoreCase(request.email().trim()).orElse(null);
        if (user == null) {
            passwordEncoder.matches(request.password(), dummyHash);
            throw wrongEmailOrPassword();
        }

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (user.isLockedAt(now)) {
            long minutes = Math.max(1, Duration.between(now, user.getLockedUntil()).toMinutes() + 1);
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS,
                    "Too many wrong passwords. Try again in about " + minutes + " minutes, or reset your password.");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            countFailedLogin(user, now);
            throw wrongEmailOrPassword();
        }
        if (!user.isActive()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "This account has been deactivated");
        }

        user.setFailedLogins(0);
        user.setLockedUntil(null);
        return new AuthResponse(tokens.issue(user), UserResponse.from(user));
    }

    /** Adds one to the wrong-password count and locks the account when the limit is reached. */
    private void countFailedLogin(User user, LocalDateTime now) {
        int failed = user.getFailedLogins() + 1;
        if (failed >= MAX_FAILED_LOGINS) {
            user.setFailedLogins(0);
            user.setLockedUntil(now.plus(LOCK_TIME));
            audit.record(user, "LOGIN_LOCKED", "Locked for " + LOCK_TIME.toMinutes() + " minutes after "
                    + MAX_FAILED_LOGINS + " wrong passwords");
        } else {
            user.setFailedLogins(failed);
        }
    }

    /** The same error for an unknown email and a wrong password, so nobody can test which emails have accounts. */
    private static ApiException wrongEmailOrPassword() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "Wrong email or password");
    }
}
