package com.snapandpack.controller;

import com.snapandpack.dto.AuthDtos.ForgotPasswordRequest;
import com.snapandpack.dto.AuthDtos.LoginRequest;
import com.snapandpack.dto.AuthDtos.MessageResponse;
import com.snapandpack.dto.AuthDtos.RegisterRequest;
import com.snapandpack.dto.AuthDtos.ResetPasswordRequest;
import com.snapandpack.dto.AuthDtos.VerifyEmailRequest;
import com.snapandpack.dto.MfaDtos.CodeRequest;
import com.snapandpack.dto.MfaDtos.DisableRequest;
import com.snapandpack.dto.MfaDtos.EnableResponse;
import com.snapandpack.dto.MfaDtos.SetupRequest;
import com.snapandpack.dto.MfaDtos.SetupResponse;
import com.snapandpack.dto.UserDtos.UserResponse;
import com.snapandpack.security.CurrentUser;
import com.snapandpack.security.SessionCookie;
import com.snapandpack.service.AuthService;
import com.snapandpack.service.AuthService.SignIn;
import com.snapandpack.service.EmailVerificationService;
import com.snapandpack.service.MfaService;
import com.snapandpack.service.PasswordResetService;
import com.snapandpack.service.UserService;
import com.snapandpack.web.SiteUrl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Addresses for registering, signing in and out, confirming an email address, two-step sign-in and resetting a forgotten password (/api/auth/...). */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final PasswordResetService passwordResetService;
    private final SessionCookie sessionCookie;
    private final SiteUrl siteUrl;
    private final MfaService mfaService;
    private final EmailVerificationService emailVerificationService;

    /** Receives the services this controller passes requests to. */
    public AuthController(AuthService authService, UserService userService, PasswordResetService passwordResetService,
                          SessionCookie sessionCookie, SiteUrl siteUrl, MfaService mfaService,
                          EmailVerificationService emailVerificationService) {
        this.mfaService = mfaService;
        this.emailVerificationService = emailVerificationService;
        this.authService = authService;
        this.userService = userService;
        this.passwordResetService = passwordResetService;
        this.sessionCookie = sessionCookie;
        this.siteUrl = siteUrl;
    }

    /** POST /api/auth/register: creates a customer account and signs it in. Open to everyone. */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request, HttpServletRequest http,
                                 HttpServletResponse response) {
        return signIn(authService.register(request, siteUrl.current()), http, response);
    }

    /** POST /api/auth/login: checks email and password and signs the user in. Open to everyone. */
    @PostMapping("/login")
    public UserResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest http,
                              HttpServletResponse response) {
        return signIn(authService.login(request), http, response);
    }

    /** POST /api/auth/logout: ends the user's sign-ins and removes the sign-in cookie. */
    @PostMapping("/logout")
    public MessageResponse logout(Authentication authentication, HttpServletRequest http, HttpServletResponse response) {
        authService.logout(CurrentUser.id(authentication));
        sessionCookie.clear(http, response);
        return new MessageResponse("Signed out");
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

    /** POST /api/auth/verify-email: confirms an email address using the code from a link. Open to everyone. */
    @PostMapping("/verify-email")
    public MessageResponse verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        emailVerificationService.verify(request.token());
        return new MessageResponse("Your email address is confirmed.");
    }

    /** POST /api/auth/resend-verification: sends the signed-in user a fresh confirmation link. */
    @PostMapping("/resend-verification")
    public MessageResponse resendVerification(Authentication authentication) {
        return new MessageResponse(emailVerificationService.resend(CurrentUser.id(authentication), siteUrl.current()));
    }

    /** POST /api/auth/mfa/setup: starts setting up two-step sign-in and returns the secret and its QR code. */
    @PostMapping("/mfa/setup")
    public SetupResponse mfaSetup(Authentication authentication, @Valid @RequestBody SetupRequest request) {
        return mfaService.beginSetup(CurrentUser.id(authentication), request.password());
    }

    /** POST /api/auth/mfa/enable: switches two-step sign-in on after a correct code, and returns the recovery codes. */
    @PostMapping("/mfa/enable")
    public EnableResponse mfaEnable(Authentication authentication, @Valid @RequestBody CodeRequest request) {
        return mfaService.enable(CurrentUser.id(authentication), request.code());
    }

    /** POST /api/auth/mfa/disable: switches two-step sign-in off for the user's own account. */
    @PostMapping("/mfa/disable")
    public MessageResponse mfaDisable(Authentication authentication, @Valid @RequestBody DisableRequest request) {
        mfaService.disable(CurrentUser.id(authentication), request.password(), request.code());
        return new MessageResponse("Two-step sign-in is off.");
    }

    /** Puts the sign-in token into its protected cookie and returns the user's details for the page to show. */
    private UserResponse signIn(SignIn signIn, HttpServletRequest http, HttpServletResponse response) {
        sessionCookie.write(http, response, signIn.token());
        return UserResponse.from(signIn.user());
    }
}
