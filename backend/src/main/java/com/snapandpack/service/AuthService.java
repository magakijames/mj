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
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository users;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokens;

    public AuthService(UserRepository users, UserService userService, PasswordEncoder passwordEncoder,
                       TokenService tokens) {
        this.users = users;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.tokens = tokens;
    }

    /** Anyone can register, but only ever as a customer. Staff accounts are created by the admin. */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        User user = userService.createAccount(request.name(), request.email(), request.password(), Role.CUSTOMER);
        return new AuthResponse(tokens.issue(user), UserResponse.from(user));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = users.findByEmailIgnoreCase(request.email().trim()).orElse(null);
        if (user == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Wrong email or password");
        }
        if (!user.isActive()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "This account has been deactivated");
        }
        return new AuthResponse(tokens.issue(user), UserResponse.from(user));
    }
}
