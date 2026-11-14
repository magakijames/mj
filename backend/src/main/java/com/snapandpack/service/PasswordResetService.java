package com.snapandpack.service;

import com.snapandpack.dto.AuthDtos.ResetLinkResponse;
import com.snapandpack.exception.ApiException;
import com.snapandpack.model.PasswordResetToken;
import com.snapandpack.model.User;
import com.snapandpack.notification.Notifier;
import com.snapandpack.repository.PasswordResetTokenRepository;
import com.snapandpack.repository.UserRepository;
import com.snapandpack.security.PasswordPolicy;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.HexFormat;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The "forgot password" feature. A reset link carries a long random code; the database keeps only
 * a hash of it. A link works once, for 30 minutes, and making a new link cancels the older ones.
 */
@Service
public class PasswordResetService {

    /** How long a reset link stays usable. */
    static final Duration LINK_LIFETIME = Duration.ofMinutes(30);

    private final PasswordResetTokenRepository tokens;
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final Notifier notifier;
    private final AuditService audit;
    private final SecureRandom random = new SecureRandom();

    /** Receives the services and database access this class needs. */
    public PasswordResetService(PasswordResetTokenRepository tokens, UserRepository users,
                                PasswordEncoder passwordEncoder, Notifier notifier, AuditService audit) {
        this.tokens = tokens;
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.notifier = notifier;
        this.audit = audit;
    }

    /**
     * Handles the "forgot password" form. When the email belongs to an active account, makes a reset link
     * and hands it to the notifier. Does nothing visible otherwise, so the reply never reveals
     * whether an email has an account.
     */
    @Transactional
    public void requestReset(String email, String siteUrl) {
        User user = users.findByEmailIgnoreCase(email.trim()).orElse(null);
        if (user == null || !user.isActive()) {
            return;
        }
        String link = createLink(user, siteUrl);
        notifier.sendPasswordReset(user, link);
        audit.record(user, "PASSWORD_RESET_REQUESTED", "Reset link requested from the sign-in page");
    }

    /** Lets the admin make a reset link for any account, to pass to that person directly. */
    @Transactional
    public ResetLinkResponse createLinkForUser(Long userId, String siteUrl) {
        User user = users.findById(userId).orElseThrow(() -> ApiException.notFound("Account not found"));
        if (!user.isActive()) {
            throw ApiException.badRequest("Activate this account before resetting its password");
        }
        String link = createLink(user, siteUrl);
        audit.record("PASSWORD_RESET_LINK_CREATED", "Reset link created for " + user.getEmail());
        return new ResetLinkResponse(link, LocalDateTime.now(ZoneOffset.UTC).plus(LINK_LIFETIME).toInstant(ZoneOffset.UTC));
    }

    /**
     * Sets a new password using the code from a reset link. Refuses a code that is unknown, used or expired.
     * On success it also unlocks the account, cancels its other reset links and ends its existing sign-ins.
     */
    @Transactional
    public void reset(String code, String newPassword) {
        PasswordPolicy.check(newPassword);
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        PasswordResetToken token = tokens.findByTokenHash(hash(code)).orElse(null);
        if (token == null || !token.isUsableAt(now) || !token.getUser().isActive()) {
            throw ApiException.badRequest("This reset link is not valid any more. Ask for a new one.");
        }

        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setPasswordChangedAt(now);
        user.setFailedLogins(0);
        user.setLockedUntil(null);
        tokens.deleteByUserId(user.getId());
        audit.record(user, "PASSWORD_RESET_COMPLETED", "Password changed with a reset link");
    }

    /** Cancels the account's older links, saves a new one and returns its full web address. */
    private String createLink(User user, String siteUrl) {
        tokens.deleteByUserId(user.getId());

        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String code = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setTokenHash(hash(code));
        token.setCreatedAt(now);
        token.setExpiresAt(now.plus(LINK_LIFETIME));
        tokens.save(token);

        return siteUrl + "/pages/reset-password.html?token=" + code;
    }

    /** Turns a reset code into the SHA-256 hash that is stored and looked up. */
    private static String hash(String code) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(code.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
