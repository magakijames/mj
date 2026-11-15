package com.snapandpack.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.snapandpack.security.CookieTokenFilter;
import com.snapandpack.security.CsrfSupport;
import com.snapandpack.security.JwtUserConverter;
import com.snapandpack.security.RequestGuardFilter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;
import org.springframework.security.web.savedrequest.NullRequestCache;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Who may call what, and the protections around every request.
 *
 * Open to everyone: the website files, browsing products, reading the site text, registering,
 * signing in, resetting a password, and the address Safaricom reports payments to.
 * Every other /api request needs a signed-in user. Role rules sit on the controller methods.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    /** Tells browsers which sources a page may load from, which blocks injected scripts from other sites. */
    private static final String CONTENT_SECURITY_POLICY = String.join("; ",
            "default-src 'self'",
            "script-src 'self'",
            "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com",
            "font-src 'self' https://fonts.gstatic.com",
            "img-src 'self' data: https:",
            "connect-src 'self'",
            "frame-ancestors 'none'",
            "base-uri 'self'",
            "form-action 'self'");

    /** The main security rules: which addresses are public, how sign-in is checked, and the safety headers. */
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, JwtDecoder jwtDecoder, JwtUserConverter jwtUserConverter,
                                    @Value("${app.rate-limit.auth-per-minute}") int authPerMinute,
                                    @Value("${app.rate-limit.api-per-minute}") int apiPerMinute,
                                    @Value("${app.max-json-body-bytes}") long maxJsonBodyBytes) throws Exception {
        http
                // Cross-site request forgery protection: see CsrfSupport. Safaricom's payment report is
                // exempt because it comes from their servers, not from a browser with a signed-in user.
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new CsrfSupport.HeaderTokenHandler())
                        .ignoringRequestMatchers("/api/payments/mpesa/callback/**"))
                .cors(Customizer.withDefaults())
                // The server keeps no session of its own: the signed cookie is checked on every request,
                // the sign-in is remembered only for the length of that request, and nothing is parked
                // in a server-side session.
                .securityContext(context -> context.securityContextRepository(new RequestAttributeSecurityContextRepository()))
                .requestCache(cache -> cache.requestCache(new NullRequestCache()))
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives(CONTENT_SECURITY_POLICY))
                        .referrerPolicy(referrer -> referrer.policy(ReferrerPolicy.SAME_ORIGIN)))
                // Not signed in: answer 401 and let the front end send the visitor to the sign-in page.
                .exceptionHandling(errors -> errors.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/login", "/api/auth/register",
                                "/api/auth/forgot-password", "/api/auth/reset-password").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products", "/api/products/**", "/api/settings").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/payments/mpesa/callback/**").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        // Everything else is the website itself (pages, styles, scripts, images, uploaded photos).
                        .anyRequest().permitAll())
                .addFilterBefore(new RequestGuardFilter(authPerMinute, apiPerMinute, maxJsonBodyBytes), CsrfFilter.class)
                .addFilterBefore(new CookieTokenFilter(jwtDecoder, jwtUserConverter), UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(new CsrfSupport.CookieFilter(), CsrfFilter.class);
        return http.build();
    }

    /**
     * The one-way hashing used for passwords. New passwords use Argon2id. Passwords saved earlier with
     * BCrypt still work, and are re-saved with Argon2id the next time their owner signs in.
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        // Salt 16 bytes, hash 32 bytes, 1 thread, 19 MB of memory, 2 passes.
        PasswordEncoder argon2 = new Argon2PasswordEncoder(16, 32, 1, 19456, 2);
        BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();
        DelegatingPasswordEncoder encoder = new DelegatingPasswordEncoder("argon2", Map.of("argon2", argon2, "bcrypt", bcrypt));
        encoder.setDefaultPasswordEncoderForMatches(bcrypt);
        return encoder;
    }

    /** Builds the secret key that signs sign-in tokens from app.jwt.secret, refusing one that is too short. */
    @Bean
    SecretKey jwtKey(@Value("${app.jwt.secret}") String secret) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("app.jwt.secret must be at least 32 characters long");
        }
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    /** Signs new tokens. */
    @Bean
    JwtEncoder jwtEncoder(SecretKey jwtKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(jwtKey));
    }

    /** Checks the signature and expiry of tokens that come in. */
    @Bean
    JwtDecoder jwtDecoder(SecretKey jwtKey) {
        return NimbusJwtDecoder.withSecretKey(jwtKey).macAlgorithm(MacAlgorithm.HS256).build();
    }

    /**
     * Lists the website addresses allowed to call the API from another port, such as VS Code Live Server,
     * and lets them send the sign-in cookie. On the live site, set app.cors.allowed-origins to the
     * site's own address only.
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.allowed-origins}") List<String> allowedOrigins) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Content-Type", "X-XSRF-TOKEN"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
