package com.snapandpack.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Protection against cross-site request forgery (another website making the user's browser send
 * a request to this one).
 *
 * The server keeps a random value in the XSRF-TOKEN cookie and also sends it in the X-XSRF-TOKEN
 * header of every answer. The site's own scripts send it back in the X-XSRF-TOKEN header of every
 * request that changes something, and the server checks the two match. Another website can neither
 * read the cookie nor see the header, so it cannot produce a matching request.
 *
 * Sending the value in a header as well as the cookie means the page never has to read the cookie
 * itself, which goes wrong when another program on the same computer has left a cookie with the same
 * name, or when the browser keeps cookies from page scripts.
 */
public final class CsrfSupport {

    /** The header the value travels in, both ways. */
    public static final String HEADER = "X-XSRF-TOKEN";

    private static final ObjectMapper JSON = new ObjectMapper();

    private CsrfSupport() {
    }

    /** Sends the current value in the X-XSRF-TOKEN header of every answer (and the cookie, when it is new). */
    public static class TokenHeaderFilter extends OncePerRequestFilter {

        /** Reads the value for this request, which also writes the cookie when needed, and adds it to the answer. */
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                throws ServletException, IOException {
            CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
            if (token != null) {
                response.setHeader(HEADER, token.getToken());
            }
            chain.doFilter(request, response);
        }
    }

    /**
     * Answers refused requests with a message the front end can show. A failed forgery check is
     * marked with "csrf": true, so the page knows to fetch a fresh value and try once more.
     */
    public static class DeniedHandler implements AccessDeniedHandler {

        /** Writes the 403 answer as JSON. */
        @Override
        public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException denied)
                throws IOException {
            Map<String, Object> body = denied instanceof CsrfException
                    ? Map.of("message", "The page's security check did not match. Reload the page and try again.", "csrf", true)
                    : Map.of("message", "You do not have permission to do that.");
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            JSON.writeValue(response.getOutputStream(), body);
        }
    }
}
