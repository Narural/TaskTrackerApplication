package com.example.TaskTracker.integration;

import com.example.TaskTracker.security.JwtService;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

class TokenAccessIntegrationTest extends AbstractIntegrationTest {

    @Value("${security.jwt.secret}")
    private String secret;

    @Test
    void request_validToken_returns200() {
        rest.get().uri("/api/tasks")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void request_withoutToken_returns401() {
        anonymous.get().uri("/api/tasks")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.code").isEqualTo("UNAUTHENTICATED");
    }

    @Test
    void request_tokenSignedWithForeignSecret_returns401() {
        String foreign = new JwtService("completely-different-secret-also-at-least-32-bytes")
                .issue(USER, List.of("USER"));

        anonymous.get().uri("/api/tasks")
                .headers(h -> h.setBearerAuth(foreign))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void request_expiredToken_returns401() {
        SecretKeySpec key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        JwtEncoder encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));

        Instant issuedAt = Instant.now().minus(Duration.ofMinutes(20));
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("task-tracker")
                .issuedAt(issuedAt)
                .expiresAt(issuedAt.plus(Duration.ofMinutes(15)))
                .subject(USER)
                .claim("roles", List.of("USER"))
                .build();
        String expired = encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();

        anonymous.get().uri("/api/tasks")
                .headers(h -> h.setBearerAuth(expired))
                .exchange()
                .expectStatus().isUnauthorized();
    }
}
