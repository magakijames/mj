package com.snapandpack.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Request and response bodies for signing in and creating a customer account. */
public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegisterRequest(
            @NotBlank(message = "Enter your name") @Size(max = 100) String name,
            @NotBlank(message = "Enter your email") @Email(message = "Enter a valid email address") @Size(max = 255) String email,
            @NotBlank(message = "Enter a password")
            @Size(min = 8, max = 72, message = "Password must be 8 to 72 characters") String password) {
    }

    public record LoginRequest(
            @NotBlank(message = "Enter your email") String email,
            @NotBlank(message = "Enter your password") String password) {
    }

    public record AuthResponse(String token, UserDtos.UserResponse user) {
    }
}
