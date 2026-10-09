package com.example.TaskTracker.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "test-secret-not-for-production-at-least-32-bytes";

    private final JwtService jwtService = new JwtService(SECRET);

    private final JwtDecoder decoder = NimbusJwtDecoder
            .withSecretKey(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"))
            .macAlgorithm(MacAlgorithm.HS256)
            .build();

    @Test
    void issue_validUser_tokenAcceptedByDecoderWithSameKey() {
        String token = jwtService.issue("kirill", List.of("USER"));

        Jwt jwt = decoder.decode(token);

        assertThat(jwt.getSubject()).isEqualTo("kirill");
        assertThat(jwt.getClaimAsStringList("roles")).containsExactly("USER");
    }

    @Test
    void issue_anyUser_expiresIn15Minutes() {
        Jwt jwt = decoder.decode(jwtService.issue("kirill", List.of("USER")));

        assertThat(Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt()))
                .isEqualTo(Duration.ofMinutes(15));
    }

    /** С коротким секретом приложение не должно стартовать: бин JwtService не создастся. */
    @Test
    void constructor_secretShorterThan32Bytes_failsFast() {
        assertThatThrownBy(() -> new JwtService("short"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT_SECRET");
    }

    /** Граница: ровно 32 байта (256 бит) — допустимо, токен выпускается. */
    @Test
    void constructor_secretExactly32Bytes_issuesToken() {
        JwtService service = new JwtService("a".repeat(32));

        assertThat(service.issue("kirill", List.of("USER"))).isNotBlank();
    }
}