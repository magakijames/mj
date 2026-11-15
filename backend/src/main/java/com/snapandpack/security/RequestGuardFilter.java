package com.snapandpack.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * First line of defence on every /api request:
 *   - refuses request bodies that are too large
 *   - limits how many requests one address may send per minute, with a much lower limit on the
 *     sign-in, register and password reset addresses to slow down password guessing
 *
 * Counts are kept in memory, which suits one server. Behind a reverse proxy such as Nginx, set
 * server.forward-headers-strategy=native so the visitor's real address is seen instead of the proxy's.
 */
public class RequestGuardFilter extends OncePerRequestFilter {

    private static final long WINDOW_MILLIS = 60_000;
    private static final int MAX_TRACKED = 20_000;

    private final int authPerMinute;
    private final int apiPerMinute;
    private final long maxBodyBytes;
    /** key = address + group, value = { start of the current minute, requests so far }. */
    private final Map<String, long[]> counters = new ConcurrentHashMap<>();

    /** Receives the limits from application.properties (app.rate-limit.* and app.max-json-body-bytes). */
    public RequestGuardFilter(int authPerMinute, int apiPerMinute, long maxBodyBytes) {
        this.authPerMinute = authPerMinute;
        this.apiPerMinute = apiPerMinute;
        this.maxBodyBytes = maxBodyBytes;
    }

    /** Only /api requests are guarded; pages, styles and images pass straight through. */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/");
    }

    /** Applies the size check and the per-minute limits, then lets the request continue. */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        boolean upload = request.getContentType() != null && request.getContentType().startsWith("multipart/");
        if (!upload && request.getContentLengthLong() > maxBodyBytes) {
            refuse(response, HttpStatus.PAYLOAD_TOO_LARGE, "That request is too large.");
            return;
        }

        String address = request.getRemoteAddr();
        boolean authAttempt = "POST".equals(request.getMethod()) && isAuthAddress(request.getRequestURI());
        if ((authAttempt && overLimit(address + "|auth", authPerMinute)) || overLimit(address + "|api", apiPerMinute)) {
            refuse(response, HttpStatus.TOO_MANY_REQUESTS, "Too many requests. Wait a minute and try again.");
            return;
        }
        chain.doFilter(request, response);
    }

    /** True for the addresses someone guessing passwords would hammer. */
    private static boolean isAuthAddress(String uri) {
        return uri.equals("/api/auth/login") || uri.equals("/api/auth/register")
                || uri.equals("/api/auth/forgot-password") || uri.equals("/api/auth/reset-password");
    }

    /** Counts one request against a key and reports whether the key has now gone past its limit for this minute. */
    private boolean overLimit(String key, int limit) {
        long now = System.currentTimeMillis();
        if (counters.size() > MAX_TRACKED) {
            counters.values().removeIf(c -> now - c[0] > WINDOW_MILLIS);
        }
        long[] counter = counters.compute(key, (k, c) ->
                c == null || now - c[0] > WINDOW_MILLIS ? new long[] {now, 1} : new long[] {c[0], c[1] + 1});
        return counter[1] > limit;
    }

    /** Sends a short JSON error and ends the request. */
    private static void refuse(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"message\":\"" + message + "\"}");
    }
}
