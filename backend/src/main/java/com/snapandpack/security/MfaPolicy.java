package com.snapandpack.security;

import com.snapandpack.model.Role;
import com.snapandpack.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Who must use two-step sign-in. While app.mfa.required-for-admins is true, a super admin or admin
 * who has not set it up yet can sign in but cannot use any staff function until they do.
 */
@Component
public class MfaPolicy {

    private static volatile boolean requiredForAdmins = true;

    /** Reads the rule from application.properties when the server starts. */
    public MfaPolicy(@Value("${app.mfa.required-for-admins}") boolean required) {
        requiredForAdmins = required;
    }

    /** True when this user is an administrator who still has to set up two-step sign-in. */
    public static boolean setupRequired(User user) {
        boolean administrator = user.getRole() == Role.SUPER_ADMIN || user.getRole() == Role.ADMIN;
        return requiredForAdmins && administrator && !user.isMfaEnabled();
    }
}
