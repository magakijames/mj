package com.snapandpack.security;

import com.snapandpack.exception.ApiException;

/** The rules every new password must meet. Used when registering, creating staff and resetting a password. */
public final class PasswordPolicy {

    public static final int MIN_LENGTH = 8;
    /** The hashing method ignores anything past 72 bytes, so longer passwords are refused. */
    public static final int MAX_LENGTH = 72;

    private PasswordPolicy() {
    }

    /** Throws an error with a clear message unless the password is 8 to 72 characters with a letter and a number. */
    public static void check(String password) {
        if (password == null || password.length() < MIN_LENGTH || password.length() > MAX_LENGTH) {
            throw ApiException.badRequest("Password must be " + MIN_LENGTH + " to " + MAX_LENGTH + " characters");
        }
        boolean hasLetter = password.chars().anyMatch(Character::isLetter);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        if (!hasLetter || !hasDigit) {
            throw ApiException.badRequest("Password must contain at least one letter and one number");
        }
    }
}
