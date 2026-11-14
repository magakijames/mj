package com.snapandpack.security;

import com.snapandpack.model.User;
import com.snapandpack.repository.UserRepository;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * Turns a valid token into a signed-in user. The role is read from the database on every request,
 * not from the token, so when the admin changes a role or deactivates an account it takes effect at once.
 */
@Component
public class JwtUserConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UserRepository users;

    /** Receives database access for accounts. */
    public JwtUserConverter(UserRepository users) {
        this.users = users;
    }

    /**
     * Looks up the account named in the token and refuses it when the account is gone, switched off,
     * or has had its password reset since the token was issued. Otherwise signs the user in with their current role.
     */
    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        User user;
        try {
            user = users.findById(Long.valueOf(jwt.getSubject())).orElse(null);
        } catch (NumberFormatException e) {
            user = null;
        }
        if (user == null || !user.isActive()) {
            throw new InvalidBearerTokenException("This account is no longer active");
        }
        if (issuedBeforePasswordChange(jwt, user)) {
            throw new InvalidBearerTokenException("The password was changed; sign in again");
        }
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        return new JwtAuthenticationToken(jwt, authorities, String.valueOf(user.getId()));
    }

    /** True when the token is older than the account's last password reset, so old sessions end at a reset. */
    private static boolean issuedBeforePasswordChange(Jwt jwt, User user) {
        if (user.getPasswordChangedAt() == null || jwt.getIssuedAt() == null) {
            return false;
        }
        // Tokens carry whole seconds, so compare against the change time rounded down to the second.
        Instant changed = user.getPasswordChangedAt().toInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);
        return jwt.getIssuedAt().isBefore(changed);
    }
}
