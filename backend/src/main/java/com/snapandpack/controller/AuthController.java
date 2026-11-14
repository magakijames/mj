package com.snapandpack.controller;

import com.snapandpack.dto.AuthDtos.AuthResponse;
import com.snapandpack.dto.AuthDtos.ForgotPasswordRequest;
import com.snapandpack.dto.AuthDtos.LoginRequest;
import com.snapandpack.dto.AuthDtos.MessageResponse;
import com.snapandpack.dto.AuthDtos.RegisterRequest;
import com.snapandpack.dto.AuthDtos.ResetPasswordRequest;
import com.snapandpack.dto.UserDtos.UserResponse;
import com.snapandpack.security.CurrentUser;
import com.snapandpack.service.AuthService;
import com.snapandpack.service.PasswordResetService;
import com.snapandpack.service.UserService;
import com.snapandpack.web.SiteUrl;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Addresses for registering, signing in and resetting a forgotten password (/api/auth/...). */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final PasswordResetService passwordResetService;
    private final SiteUrl siteUrl;

    /** Receives the services this controller passes requests to. */
    public AuthController(AuthService authService, UserService userService,
                          PasswordResetService passwordResetService, SiteUrl siteUrl) {
        this.authService = authService;
        this.userService = userService;
        this.passwordResetService = passwordResetService;
        this.siteUrl = siteUrl;
    }

    /** POST /api/auth/register: creates a customer account and signs it in. Open to everyone. */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    /** POST /api/auth/login: checks email and password and returns a sign-in token. Open to everyone. */
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    /** GET /api/auth/me: the signed-in user's own details, with their current role. */
    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {
        return userService.get(CurrentUser.id(authentication));
    }

    /**
     * POST /api/auth/forgot-password: starts a password reset. Open to everyone.
     * The reply is the same whether or not the email has an account.
     */
    @PostMapping("/forgot-password")
    public MessageResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.requestReset(request.email(), siteUrl.current());
        return new MessageResponse("If that email has an account, a reset link has been created for it.");
    }

    /** POST /api/auth/reset-password: sets a new password using the code from a reset link. Open to everyone. */
    @PostMapping("/reset-password")
    public MessageResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.reset(request.token(), request.password());
        return new MessageResponse("Your password has been changed. You can sign in now.");
    }
}
