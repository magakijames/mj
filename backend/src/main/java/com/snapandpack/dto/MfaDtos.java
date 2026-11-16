package com.snapandpack.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

/** Request and response bodies for two-step sign-in (MFA). */
public final class MfaDtos {

    private MfaDtos() {
    }

    /** Starting set-up: the user confirms their password first. */
    public record SetupRequest(@NotBlank(message = "Enter your password") String password) {
    }

    /** What the set-up page shows: the secret as text, as a link for phones, and as a QR code picture. */
    public record SetupResponse(String secret, String otpauthUri, String qrImage) {
    }

    /** A code typed from the authenticator app. */
    public record CodeRequest(@NotBlank(message = "Enter the 6-digit code") @Size(max = 20) String code) {
    }

    /** Sent once, when two-step sign-in is switched on: the recovery codes to keep somewhere safe. */
    public record EnableResponse(List<String> recoveryCodes) {
    }

    /** Switching two-step sign-in off needs both the password and a current code. */
    public record DisableRequest(
            @NotBlank(message = "Enter your password") String password,
            @NotBlank(message = "Enter a code from your authenticator app") @Size(max = 20) String code) {
    }
}
