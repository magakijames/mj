package com.snapandpack.service;

import com.snapandpack.dto.MfaDtos.EnableResponse;
import com.snapandpack.dto.MfaDtos.SetupResponse;
import com.snapandpack.exception.ApiException;
import com.snapandpack.model.MfaRecoveryCode;
import com.snapandpack.model.User;
import com.snapandpack.repository.MfaRecoveryCodeRepository;
import com.snapandpack.repository.UserRepository;
import com.snapandpack.security.Hashing;
import com.snapandpack.security.QrCode;
import com.snapandpack.security.SecretCipher;
import com.snapandpack.security.Totp;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Two-step sign-in (multi-factor authentication). After the password, the user types a 6-digit code
 * from an authenticator app on their phone. Anyone can switch it on for their own account;
 * super admins and admins are required to (see MfaPolicy).
 */
@Service
public class MfaService {

    private static final int RECOVERY_CODES = 8;
    /** Letters and digits that are hard to mix up when read aloud or copied by hand. */
    private static final String RECOVERY_ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";

    private final UserRepository users;
    private final MfaRecoveryCodeRepository recoveryCodes;
    private final PasswordEncoder passwordEncoder;
    private final SecretCipher cipher;
    private final AuditService audit;
    private final NotificationService notifications;
    private final String issuer;

    /** Receives the services and database access this class needs, and the name shown in the authenticator app. */
    public MfaService(UserRepository users, MfaRecoveryCodeRepository recoveryCodes, PasswordEncoder passwordEncoder,
                      SecretCipher cipher, AuditService audit, NotificationService notifications,
                      @Value("${app.mfa.issuer}") String issuer) {
        this.users = users;
        this.recoveryCodes = recoveryCodes;
        this.passwordEncoder = passwordEncoder;
        this.cipher = cipher;
        this.audit = audit;
        this.notifications = notifications;
        this.issuer = issuer;
    }

    /**
     * Step 1 of switching on: checks the password, makes a new secret and returns it as text and as
     * a QR code for the authenticator app. Nothing is switched on until a code is confirmed in step 2.
     */
    @Transactional
    public SetupResponse beginSetup(Long userId, String password) {
        User user = find(userId);
        requirePassword(user, password);
        if (user.isMfaEnabled()) {
            throw ApiException.conflict("Two-step sign-in is already on. Switch it off first to set it up again.");
        }
        String secret = Totp.newSecret();
        user.setMfaSecret(cipher.encrypt(secret));
        user.setMfaLastStep(null);

        String label = encode(issuer + ":" + user.getEmail());
        String uri = "otpauth://totp/" + label + "?secret=" + secret + "&issuer=" + encode(issuer)
                + "&algorithm=SHA1&digits=6&period=30";
        return new SetupResponse(secret, uri, QrCode.asDataUri(uri));
    }

    /**
     * Step 2 of switching on: the user proves the app is set up by typing a code from it.
     * Returns the recovery codes, which are shown this once and never again.
     */
    @Transactional
    public EnableResponse enable(Long userId, String code) {
        User user = find(userId);
        if (user.isMfaEnabled()) {
            throw ApiException.conflict("Two-step sign-in is already on.");
        }
        if (user.getMfaSecret() == null) {
            throw ApiException.badRequest("Start the set-up first.");
        }
        long step = Totp.matchingStep(cipher.decrypt(user.getMfaSecret()), digits(code));
        if (step < 0) {
            throw ApiException.badRequest("That code is not right. Check the newest code in your app and try again.");
        }
        user.setMfaEnabled(true);
        user.setMfaLastStep(step);
        audit.record(user, "MFA_ENABLED", "Two-step sign-in switched on");
        return new EnableResponse(newRecoveryCodes(user));
    }

    /** Switches two-step sign-in off for the user's own account. Needs the password and a current code. */
    @Transactional
    public void disable(Long userId, String password, String code) {
        User user = find(userId);
        requirePassword(user, password);
        if (!user.isMfaEnabled()) {
            throw ApiException.badRequest("Two-step sign-in is not on.");
        }
        if (!verifyAtSignIn(user, code)) {
            throw ApiException.badRequest("That code is not right.");
        }
        clear(user);
        audit.record(user, "MFA_DISABLED", "Two-step sign-in switched off");
    }

    /**
     * Lets the super admin remove two-step sign-in from someone else's account, for a member of
     * staff who lost their phone and their recovery codes. That person must then set it up again.
     */
    @Transactional
    public void resetFor(Long actingAdminId, Long userId) {
        if (actingAdminId.equals(userId)) {
            throw ApiException.badRequest("Use your own Account page to change your two-step sign-in.");
        }
        User user = find(userId);
        clear(user);
        audit.record("MFA_RESET", "Two-step sign-in removed for " + user.getEmail());
        notifications.notify(user, "Two-step sign-in was reset",
                "An administrator removed two-step sign-in from your account. Set it up again from your Account page.",
                "pages/account.html");
    }

    /**
     * Checks the second step at sign-in. Accepts either a 6-digit code from the app, which must not
     * have been used before, or one of the recovery codes, each of which works once.
     */
    @Transactional
    public boolean verifyAtSignIn(User user, String code) {
        String typed = code == null ? "" : code.trim();
        if (digits(typed).matches("\\d{6}")) {
            long step = Totp.matchingStep(cipher.decrypt(user.getMfaSecret()), digits(typed));
            if (step < 0 || (user.getMfaLastStep() != null && step <= user.getMfaLastStep())) {
                return false;
            }
            user.setMfaLastStep(step);
            return true;
        }
        String normalised = typed.toUpperCase().replaceAll("[^A-Z0-9]", "");
        MfaRecoveryCode recovery = recoveryCodes
                .findFirstByUserIdAndCodeHashAndUsedAtIsNull(user.getId(), Hashing.sha256Hex(normalised)).orElse(null);
        if (recovery == null) {
            return false;
        }
        recovery.setUsedAt(LocalDateTime.now(ZoneOffset.UTC));
        long left = recoveryCodes.countByUserIdAndUsedAtIsNull(user.getId()) - 1;
        audit.record(user, "MFA_RECOVERY_CODE_USED", Math.max(0, left) + " recovery codes left");
        return true;
    }

    /** Replaces the user's recovery codes with a fresh set and returns them as text, such as ABCD-EFGH. */
    private List<String> newRecoveryCodes(User user) {
        recoveryCodes.deleteByUserId(user.getId());
        List<String> plain = new ArrayList<>();
        for (int i = 0; i < RECOVERY_CODES; i++) {
            StringBuilder code = new StringBuilder();
            for (byte b : Hashing.randomBytes(8)) {
                code.append(RECOVERY_ALPHABET.charAt((b & 0xFF) % RECOVERY_ALPHABET.length()));
            }
            MfaRecoveryCode stored = new MfaRecoveryCode();
            stored.setUserId(user.getId());
            stored.setCodeHash(Hashing.sha256Hex(code.toString()));
            recoveryCodes.save(stored);
            plain.add(code.substring(0, 4) + "-" + code.substring(4));
        }
        return plain;
    }

    /** Removes the secret and recovery codes and switches two-step sign-in off. */
    private void clear(User user) {
        user.setMfaEnabled(false);
        user.setMfaSecret(null);
        user.setMfaLastStep(null);
        recoveryCodes.deleteByUserId(user.getId());
    }

    /** Fails unless the password is the user's own. */
    private void requirePassword(User user, String password) {
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw ApiException.badRequest("That password is not right.");
        }
    }

    /** Removes spaces from a typed code, so "123 456" is read as 123456. */
    private static String digits(String code) {
        return code == null ? "" : code.replaceAll("\\s", "");
    }

    /** Makes text safe to put inside the otpauth link. */
    private static String encode(String text) {
        return URLEncoder.encode(text, StandardCharsets.UTF_8).replace("+", "%20");
    }

    /** Loads an account or fails with "not found". */
    private User find(Long id) {
        return users.findById(id).orElseThrow(() -> ApiException.notFound("Account not found"));
    }
}
