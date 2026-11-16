package com.snapandpack.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * A one-time code for signing in when the authenticator app is not available (a lost phone).
 * Only a hash of each code is stored, and each works once.
 */
@Entity
@Table(name = "mfa_recovery_codes")
public class MfaRecoveryCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** SHA-256 of the code, as 64 hex characters. */
    @Column(name = "code_hash", nullable = false, columnDefinition = "char(64)")
    private String codeHash;

    /** Set when the code has been used. */
    @Column(name = "used_at")
    private LocalDateTime usedAt;

    // ---- Getters and setters: plain read and write access to the fields above. ----

    public Long getId() { return id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getCodeHash() { return codeHash; }
    public void setCodeHash(String codeHash) { this.codeHash = codeHash; }

    public LocalDateTime getUsedAt() { return usedAt; }
    public void setUsedAt(LocalDateTime usedAt) { this.usedAt = usedAt; }
}
