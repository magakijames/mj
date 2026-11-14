package com.snapandpack.dto;

import com.snapandpack.model.Role;
import com.snapandpack.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/** Request and response bodies for accounts. The password hash is never sent out. */
public final class UserDtos {

    private UserDtos() {
    }

    /** An account as the front end sees it. */
    public record UserResponse(Long id, String name, String email, Role role, boolean active, boolean locked,
                               Instant createdAt) {

        /** Copies the safe fields of an account into a response. */
        public static UserResponse from(User user) {
            return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.isActive(),
                    user.isLockedAt(LocalDateTime.now(ZoneOffset.UTC)), user.getCreatedAt().toInstant(ZoneOffset.UTC));
        }
    }

    /** Used by the admin to create an account with any role. */
    public record CreateUserRequest(
            @NotBlank(message = "Enter a name") @Size(max = 100) String name,
            @NotBlank(message = "Enter an email") @Email(message = "Enter a valid email address") @Size(max = 255) String email,
            @NotBlank(message = "Enter a password") String password,
            @NotNull(message = "Choose a role") Role role) {
    }

    /** Used by the admin to change what an account may do. */
    public record UpdateRoleRequest(@NotNull(message = "Choose a role") Role role) {
    }

    /** Used by the admin to switch an account on or off. */
    public record UpdateActiveRequest(@NotNull Boolean active) {
    }
}
