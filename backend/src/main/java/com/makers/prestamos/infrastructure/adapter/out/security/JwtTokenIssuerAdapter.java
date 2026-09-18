package com.makers.prestamos.infrastructure.adapter.out.security;

import com.makers.prestamos.application.port.out.TokenIssuerPort;
import com.makers.prestamos.domain.model.User;
import com.makers.prestamos.infrastructure.config.JwtProperties;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;

@Component
class JwtTokenIssuerAdapter implements TokenIssuerPort {

    private final JwtEncoder encoder;
    private final JwtProperties props;
    private final Clock clock;

    JwtTokenIssuerAdapter(JwtEncoder encoder, JwtProperties props, Clock clock) {
        this.encoder = encoder;
        this.props = props;
        this.clock = clock;
    }

    @Override
    public IssuedToken issue(User user) {
        Instant now = clock.instant();
        Instant expiresAt = now.plus(props.expiration());

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(props.issuer())
                .subject(String.valueOf(user.id()))
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim("email", user.email())
                .claim("role", user.role().name())
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new IssuedToken(token, expiresAt);
    }
}
