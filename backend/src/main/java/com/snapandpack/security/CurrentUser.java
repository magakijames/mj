package com.snapandpack.security;

import org.springframework.security.core.Authentication;

public final class CurrentUser {

    private CurrentUser() {
    }

    /** The id of the signed-in user. JwtUserConverter stores it as the authentication name. */
    public static Long id(Authentication authentication) {
        return Long.valueOf(authentication.getName());
    }
}
