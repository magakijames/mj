package com.snapandpack.service;

import com.snapandpack.dto.AuthDtos.LoginRequest;
import com.snapandpack.dto.AuthDtos.RegisterRequest;
import com.snapandpack.exception.ApiException;
import com.snapandpack.exception.MfaRequiredException;
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

/** Registering, signing in and signing out, including the lock that stops password guessing. */
@Service
public class AuthService {

    /** Wrong passwords allowed in a row before the account is locked. */
    static final int MAX_FAILED_LOGINS = 5;
    /** How long a locked account stays locked. */
    static final Duration LOCK_TIME = Duration.ofMinutes(15);

    /** The result of a successful sign-in: who it is, and the token to put in their cookie. */
    public record SignIn(User user, String token) {
    }

    private final UserRepository users;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokens;
    private final AuditService audit;
    private final MfaService mfa;
    private final EmailVerificationService emailVerification;
    /** Checked when the email is unknown, so a wrong email takes as long to answer as a wrong password. */
    private final String dummyHash;

    /** Receives the services this class needs and prepares the stand-in hash for unknown emails. */
    public AuthService(UserRepository users, UserService userService, PasswordEncoder passwordEncoder,
                       TokenService tokens, AuditService audit, MfaService mfa,
                       EmailVerificationService emailVerification) {
        this.users = users;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.tokens = tokens;
        this.audit = audit;
        this.mfa = mfa;
        this.emailVerification = emailVerification;
        this.dummyHash = passwordEncoder.encode("not-a-real-password");
    }

    /**
     * Creates a customer account, sends the link for confirming its email address and signs it in.
     * Staff accounts can only be created by the super admin.
     */
    @Transactional
    public SignIn register(RegisterRequest request, String siteUrl) {
        User user = userService.createAccount(request.name(), request.email(), request.password(), Role.CUSTOMER, false);
        audit.record(user, "ACCOUNT_REGISTERED", "Customer account created");
        emailVerification.send(user, siteUrl);
        return new SignIn(user, tokens.issue(user));
    }

    /**
     * Checks the email and password, and the authenticator code for accounts that use one,
     * and returns a sign-in token.
     * After five wrong passwords in a row the account is locked for 15 minutes.
     * Every attempt, good or bad, is written to the activity record, and the failed-attempt
     * count is saved even though the request ends in an error.
     */
    @Transactional(noRollbackFor = ApiException.class)
    public SignIn login(LoginRequest request) {
        String email = request.email().trim();
        User user = users.findByEmailIgnoreCase(email).orElse(null);
        if (user == null) {
            passwordEncoder.matches(request.password(), dummyHash);
            audit.record((User) null, "LOGIN_FAILED", "Unknown email: " + (email.length() > 100 ? email.substring(0, 100) : email));
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
            audit.record(user, "LOGIN_FAILED", "Account is deactivated");
            throw new ApiException(HttpStatus.FORBIDDEN, "This account has been deactivated");
        }

        // Second step, for accounts with two-step sign-in: a code from the authenticator app.
        if (user.isMfaEnabled()) {
            if (request.code() == null || request.code().isBlank()) {
                throw new MfaRequiredException();
            }
            if (!mfa.verifyAtSignIn(user, request.code())) {
                countFailedLogin(user, now);
                throw new ApiException(HttpStatus.UNAUTHORIZED, "That code is not right. Use the newest code in your app.");
            }
        }

        // Passwords saved with the older hashing method are re-saved with the current one at sign-in.
        if (passwordEncoder.upgradeEncoding(user.getPasswordHash())) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }
        user.setFailedLogins(0);
        user.setLockedUntil(null);
        audit.record(user, "LOGIN_SUCCESS", "Signed in");
        return new SignIn(user, tokens.issue(user));
    }

    /** Signs a user out everywhere: every token issued to them before this moment stops working. */
    @Transactional
    public void logout(Long userId) {
        users.findById(userId).ifPresent(user -> {
            user.setSessionsValidAfter(LocalDateTime.now(ZoneOffset.UTC));
            audit.record(user, "LOGOUT", "Signed out");
        });
    }

    /** Adds one to the count of wrong passwords and codes, and locks the account when the limit is reached. */
    private void countFailedLogin(User user, LocalDateTime now) {
        int failed = user.getFailedLogins() + 1;
        if (failed >= MAX_FAILED_LOGINS) {
            user.setFailedLogins(0);
            user.setLockedUntil(now.plus(LOCK_TIME));
            audit.record(user, "LOGIN_LOCKED", "Locked for " + LOCK_TIME.toMinutes() + " minutes after "
                    + MAX_FAILED_LOGINS + " wrong passwords");
        } else {
            user.setFailedLogins(failed);
            audit.record(user, "LOGIN_FAILED", "Wrong password or code (" + failed + " of " + MAX_FAILED_LOGINS + ")");
        }
    }

    /** The same error for an unknown email and a wrong password, so nobody can test which emails have accounts. */
    private static ApiException wrongEmailOrPassword() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "Wrong email or password");
    }
}
