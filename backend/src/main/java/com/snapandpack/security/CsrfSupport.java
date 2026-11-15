package com.snapandpack.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.function.Supplier;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Protection against cross-site request forgery (another website making the user's browser send
 * a request to this one). The server hands the browser a random value in the XSRF-TOKEN cookie;
 * the site's own scripts copy it into the X-XSRF-TOKEN header of every request that changes something.
 * Another website cannot read that cookie, so it cannot produce the header, and its request is refused.
 */
public final class CsrfSupport {

    private CsrfSupport() {
    }

    /** Accepts the value sent back in the X-XSRF-TOKEN header by the site's own scripts. */
    public static class HeaderTokenHandler implements CsrfTokenRequestHandler {

        private final CsrfTokenRequestHandler plain = new CsrfTokenRequestAttributeHandler();
        private final CsrfTokenRequestHandler masked = new XorCsrfTokenRequestAttributeHandler();

        /** Makes the token available to the rest of the request. */
        @Override
        public void handle(HttpServletRequest request, HttpServletResponse response, Supplier<CsrfToken> token) {
            masked.handle(request, response, token);
        }

        /** Reads the token the request sent: from the header when present, otherwise from a form field. */
        @Override
        public String resolveCsrfTokenValue(HttpServletRequest request, CsrfToken token) {
            boolean inHeader = StringUtils.hasText(request.getHeader(token.getHeaderName()));
            return (inHeader ? plain : masked).resolveCsrfTokenValue(request, token);
        }
    }

    /** Makes sure the XSRF-TOKEN cookie is sent on every response, so the site's scripts always have it. */
    public static class CookieFilter extends OncePerRequestFilter {

        /** Touches the token, which causes the cookie to be written, then lets the request continue. */
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                throws ServletException, IOException {
            CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
            if (token != null) {
                token.getToken();
            }
            chain.doFilter(request, response);
        }
    }
}
