package com.snapandpack.security;

import com.snapandpack.model.User;
import com.snapandpack.repository.UserRepository;
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

    public JwtUserConverter(UserRepository users) {
        this.users = users;
    }

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
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        return new JwtAuthenticationToken(jwt, authorities, String.valueOf(user.getId()));
    }
}
