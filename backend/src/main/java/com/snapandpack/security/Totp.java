package com.snapandpack.security;

import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Time-based one-time codes: the 6-digit codes shown by authenticator apps such as Google
 * Authenticator, Microsoft Authenticator and Authy (the TOTP standard, RFC 6238).
 * The app and the server share a secret; both turn "the secret + the current half-minute"
 * into the same 6 digits, so a matching code proves the person holds the phone.
 */
public final class Totp {

    private static final int STEP_SECONDS = 30;
    private static final int DIGITS = 6;
    private static final String BASE32 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

    private Totp() {
    }

    /** Makes a new random secret, written in the letters-and-digits form authenticator apps accept. */
    public static String newSecret() {
        return base32Encode(Hashing.randomBytes(20));
    }

    /** The number of the current 30-second step. */
    public static long currentStep() {
        return Instant.now().getEpochSecond() / STEP_SECONDS;
    }

    /**
     * Checks a 6-digit code against a secret. The step before and after the current one are also
     * accepted, to allow for a phone clock that is a little off.
     * Returns the step the code belongs to, or -1 when the code is wrong.
     */
    public static long matchingStep(String secret, String code) {
        byte[] key = base32Decode(secret);
        long now = currentStep();
        for (long step = now - 1; step <= now + 1; step++) {
            byte[] expected = codeAt(key, step).getBytes(StandardCharsets.UTF_8);
            if (MessageDigest.isEqual(expected, code.getBytes(StandardCharsets.UTF_8))) {
                return step;
            }
        }
        return -1;
    }

    /** Works out the 6-digit code for one 30-second step, as the standard describes. */
    static String codeAt(byte[] key, long step) {
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(key, "HmacSHA1"));
            byte[] hash = mac.doFinal(ByteBuffer.allocate(8).putLong(step).array());
            int offset = hash[hash.length - 1] & 0x0F;
            int binary = ((hash[offset] & 0x7F) << 24) | ((hash[offset + 1] & 0xFF) << 16)
                    | ((hash[offset + 2] & 0xFF) << 8) | (hash[offset + 3] & 0xFF);
            return String.format("%0" + DIGITS + "d", binary % 1_000_000);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Could not work out a sign-in code", e);
        }
    }

    /** Writes bytes in Base32, the alphabet authenticator apps use for secrets. */
    static String base32Encode(byte[] data) {
        StringBuilder out = new StringBuilder();
        int buffer = 0;
        int bits = 0;
        for (byte b : data) {
            buffer = (buffer << 8) | (b & 0xFF);
            bits += 8;
            while (bits >= 5) {
                out.append(BASE32.charAt((buffer >> (bits - 5)) & 31));
                bits -= 5;
            }
        }
        if (bits > 0) {
            out.append(BASE32.charAt((buffer << (5 - bits)) & 31));
        }
        return out.toString();
    }

    /** Reads Base32 text back into bytes. Characters outside the alphabet are skipped. */
    static byte[] base32Decode(String text) {
        ByteBuffer out = ByteBuffer.allocate(text.length() * 5 / 8);
        int buffer = 0;
        int bits = 0;
        for (char c : text.toUpperCase().toCharArray()) {
            int value = BASE32.indexOf(c);
            if (value < 0) {
                continue;
            }
            buffer = (buffer << 5) | value;
            bits += 5;
            if (bits >= 8) {
                out.put((byte) ((buffer >> (bits - 8)) & 0xFF));
                bits -= 8;
            }
        }
        byte[] result = new byte[out.position()];
        out.flip();
        out.get(result);
        return result;
    }
}
