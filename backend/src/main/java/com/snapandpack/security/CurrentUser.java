package com.snapandpack.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/** Helpers for finding out who is making the current request. */
public final class CurrentUser {

    private CurrentUser() {
    }

    /** The id of the signed-in user. JwtUserConverter stores it as the authentication name. */
    public static Long id(Authentication authentication) {
        return Long.valueOf(authentication.getName());
    }

    /** The id of whoever is signed in on the current request, or null when nobody is. */
    public static Long idOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication instanceof JwtAuthenticationToken ? id(authentication) : null;
    }
}
