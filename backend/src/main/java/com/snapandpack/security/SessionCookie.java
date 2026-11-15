package com.snapandpack.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/**
 * The cookie that keeps a user signed in. It holds the signed token and is:
 *   HttpOnly  - page scripts cannot read it, so a script injected into a page cannot steal the sign-in
 *   SameSite  - the browser does not send it on requests started by other websites
 *   Secure    - sent over HTTPS only (switched on with app.cookies.secure=true once the site has HTTPS)
 */
@Component
public class SessionCookie {

    public static final String NAME = "sp_session";

    private final boolean secure;
    private final Duration lifetime;

    /** Receives app.cookies.secure and app.jwt.hours-valid from application.properties. */
    public SessionCookie(@Value("${app.cookies.secure}") boolean secure, @Value("${app.jwt.hours-valid}") long hoursValid) {
        this.secure = secure;
        this.lifetime = Duration.ofHours(hoursValid);
    }

    /** Adds the sign-in cookie to a response, after a successful sign-in or registration. */
    public void write(HttpServletResponse response, String token) {
        response.addHeader(HttpHeaders.SET_COOKIE, build(token, lifetime).toString());
    }

    /** Tells the browser to delete the sign-in cookie, when signing out. */
    public void clear(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, build("", Duration.ZERO).toString());
    }

    /** Reads the token from the request's sign-in cookie, or null when there is none. */
    public static String read(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (NAME.equals(cookie.getName()) && !cookie.getValue().isEmpty()) {
                return cookie.getValue();
            }
        }
        return null;
    }

    /** Builds the cookie with its protective settings. Only /api requests carry it. */
    private ResponseCookie build(String value, Duration maxAge) {
        return ResponseCookie.from(NAME, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Strict")
                .path("/api")
                .maxAge(maxAge)
                .build();
    }
}
