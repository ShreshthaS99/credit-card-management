package com.example.ccmps.security;

import java.time.Instant;
import java.util.UUID;
import javax.crypto.SecretKey;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Service;

@Service
public class TokenService {
    private final NimbusJwtEncoder encoder;
    private final String issuer;
    private final String audience;
    private final long expirationSeconds;

    public TokenService(SecretKey key, @Value("${app.jwt.issuer}") String issuer,
                        @Value("${app.jwt.audience}") String audience,
                        @Value("${app.jwt.expiration-seconds}") long expirationSeconds) {
        this.encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
        this.issuer = issuer;
        this.audience = audience;
        this.expirationSeconds = expirationSeconds;
    }

    public String issue(String username, String role, UUID customerId) {
        Instant now = Instant.now();
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer(issuer).audience(java.util.List.of(audience))
                .subject(username).issuedAt(now).expiresAt(now.plusSeconds(expirationSeconds))
                .claim("role", role);
        if (customerId != null) {
            claims.claim("customerId", customerId.toString());
        }
        return encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims.build())).getTokenValue();
    }

    public String provisioningToken() {
        return "Bearer " + issue("auth-service", "SYSTEM", null);
    }
}
