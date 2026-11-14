package com.snapandpack.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

/** Request and response bodies for signing in, registering and resetting a password. */
public final class AuthDtos {

    private AuthDtos() {
    }

    /** What the register form sends. */
    public record RegisterRequest(
            @NotBlank(message = "Enter your name") @Size(max = 100) String name,
            @NotBlank(message = "Enter your email") @Email(message = "Enter a valid email address") @Size(max = 255) String email,
            @NotBlank(message = "Enter a password") String password) {
    }

    /** What the sign-in form sends. */
    public record LoginRequest(
            @NotBlank(message = "Enter your email") String email,
            @NotBlank(message = "Enter your password") String password) {
    }

    /** Sent back after a successful sign-in or registration: the token and who signed in. */
    public record AuthResponse(String token, UserDtos.UserResponse user) {
    }

    /** What the "forgot password" form sends. */
    public record ForgotPasswordRequest(
            @NotBlank(message = "Enter your email") @Size(max = 255) String email) {
    }

    /** What the "choose a new password" form sends: the code from the link and the new password. */
    public record ResetPasswordRequest(
            @NotBlank(message = "This reset link is not valid") @Size(max = 200) String token,
            @NotBlank(message = "Enter a new password") String password) {
    }

    /** A reset link the admin can pass to a user, and when it stops working. */
    public record ResetLinkResponse(String link, Instant expiresAt) {
    }

    /** A plain message for the front end to show. */
    public record MessageResponse(String message) {
    }
}
