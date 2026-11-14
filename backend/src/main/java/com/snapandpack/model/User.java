package com.snapandpack.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/** One account: a customer or a member of staff. Maps to the "users" table. */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    /** Always stored in lower case. */
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    /** The password after one-way hashing. The real password is never stored. */
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    /** False once the admin has switched the account off. */
    @Column(name = "active", nullable = false)
    private boolean active = true;

    /** Wrong passwords in a row since the last successful sign-in. */
    @Column(name = "failed_logins", nullable = false)
    private int failedLogins;

    /** While this time (UTC) is in the future, sign-in is refused. Null when not locked. */
    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    /** When the password was last reset (UTC). Sign-in tokens issued before this stop working. */
    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    /** Stored in UTC. */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /** True while the account is temporarily locked after too many wrong passwords. */
    public boolean isLockedAt(LocalDateTime now) {
        return lockedUntil != null && lockedUntil.isAfter(now);
    }

    // ---- Getters and setters: plain read and write access to the fields above. ----

    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public int getFailedLogins() { return failedLogins; }
    public void setFailedLogins(int failedLogins) { this.failedLogins = failedLogins; }

    public LocalDateTime getLockedUntil() { return lockedUntil; }
    public void setLockedUntil(LocalDateTime lockedUntil) { this.lockedUntil = lockedUntil; }

    public LocalDateTime getPasswordChangedAt() { return passwordChangedAt; }
    public void setPasswordChangedAt(LocalDateTime passwordChangedAt) { this.passwordChangedAt = passwordChangedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
