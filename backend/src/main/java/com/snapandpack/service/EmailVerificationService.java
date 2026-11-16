package com.snapandpack.service;

import com.snapandpack.exception.ApiException;
import com.snapandpack.model.EmailVerificationToken;
import com.snapandpack.model.User;
import com.snapandpack.notification.EmailNotifier;
import com.snapandpack.notification.Notifier;
import com.snapandpack.repository.EmailVerificationTokenRepository;
import com.snapandpack.repository.UserRepository;
import com.snapandpack.security.Hashing;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Confirming that a new account's email address really belongs to the person who registered.
 * A link with a long random code is sent to the address; opening it confirms the account.
 * The database keeps only a hash of the code, and a link works for 24 hours.
 */
@Service
public class EmailVerificationService {

    /** How long a confirmation link stays usable. */
    static final Duration LINK_LIFETIME = Duration.ofHours(24);
    /** The shortest wait between two links for the same account. */
    static final Duration RESEND_WAIT = Duration.ofSeconds(60);

    private final EmailVerificationTokenRepository tokens;
    private final UserRepository users;
    private final List<Notifier> channels;
    private final boolean emailConnected;
    private final AuditService audit;

    /** Receives the services and database access this class needs, and notes whether email is switched on. */
    public EmailVerificationService(EmailVerificationTokenRepository tokens, UserRepository users,
                                    List<Notifier> channels, ObjectProvider<EmailNotifier> emailNotifier,
                                    AuditService audit) {
        this.tokens = tokens;
        this.users = users;
        this.channels = channels;
        this.emailConnected = emailNotifier.getIfAvailable() != null;
        this.audit = audit;
    }

    /** Makes a confirmation link for an account and sends it through every channel that is switched on. */
    @Transactional
    public void send(User user, String siteUrl) {
        tokens.deleteByUserId(user.getId());
        String code = Hashing.randomUrlCode();
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        EmailVerificationToken token = new EmailVerificationToken();
        token.setUser(user);
        token.setTokenHash(Hashing.sha256Hex(code));
        token.setCreatedAt(now);
        token.setExpiresAt(now.plus(LINK_LIFETIME));
        tokens.save(token);

        String link = siteUrl + "/pages/verify-email.html?token=" + code;
        for (Notifier channel : channels) {
            channel.send(user, "Confirm your email address",
                    "Open this link to confirm your email address. It works for 24 hours:\n" + link);
        }
    }

    /** Sends a fresh link to the signed-in user, at most once a minute. Returns the message to show them. */
    @Transactional
    public String resend(Long userId, String siteUrl) {
        User user = users.findById(userId).orElseThrow(() -> ApiException.notFound("Account not found"));
        if (user.isEmailVerified()) {
            return "Your email address is already confirmed.";
        }
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        boolean tooSoon = tokens.findFirstByUserIdOrderByIdDesc(userId)
                .map(last -> last.getCreatedAt().plus(RESEND_WAIT).isAfter(now)).orElse(false);
        if (tooSoon) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "A link was sent a moment ago. Wait a minute before asking again.");
        }
        send(user, siteUrl);
        return emailConnected
                ? "We sent a new confirmation link to " + user.getEmail() + "."
                : "A new confirmation link was created. Email is not connected yet, so ask the shop for your link.";
    }

    /** Confirms an account using the code from a link. Refuses a code that is unknown or has expired. */
    @Transactional
    public void verify(String code) {
        EmailVerificationToken token = tokens.findByTokenHash(Hashing.sha256Hex(code)).orElse(null);
        if (token == null || !token.getExpiresAt().isAfter(LocalDateTime.now(ZoneOffset.UTC))) {
            throw ApiException.badRequest("This confirmation link is not valid any more. Sign in and ask for a new one.");
        }
        User user = token.getUser();
        user.setEmailVerified(true);
        tokens.deleteByUserId(user.getId());
        audit.record(user, "EMAIL_VERIFIED", "Email address confirmed with a link");
    }

    /** Lets the super admin confirm an account's email by hand, for example while email is not connected. */
    @Transactional
    public void markVerified(Long userId) {
        User user = users.findById(userId).orElseThrow(() -> ApiException.notFound("Account not found"));
        user.setEmailVerified(true);
        tokens.deleteByUserId(userId);
        audit.record("EMAIL_VERIFIED_BY_ADMIN", user.getEmail());
    }
}
