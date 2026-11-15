package com.snapandpack.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Runs on every request. If the request carries a sign-in cookie with a valid token, the user is
 * treated as signed in for this request. A missing, expired or forged token just means "not signed in".
 */
public class CookieTokenFilter extends OncePerRequestFilter {

    private final JwtDecoder decoder;
    private final JwtUserConverter converter;
    private final SecurityContextRepository contextRepository = new RequestAttributeSecurityContextRepository();

    /** Receives the token checker and the class that turns a token into a user. */
    public CookieTokenFilter(JwtDecoder decoder, JwtUserConverter converter) {
        this.decoder = decoder;
        this.converter = converter;
    }

    /** Checks the sign-in cookie, signs the user in for this request if it is good, then lets the request continue. */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String token = SessionCookie.read(request);
        if (token != null) {
            try {
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(converter.convert(decoder.decode(token)));
                SecurityContextHolder.setContext(context);
                // Records the sign-in on the request so later steps in the same request can find it.
                contextRepository.saveContext(context, request, response);
            } catch (RuntimeException e) {
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(request, response);
    }
}
