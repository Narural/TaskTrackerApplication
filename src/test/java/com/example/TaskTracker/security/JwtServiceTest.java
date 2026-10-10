package com.example.TaskTracker.security;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET = "test-secret-not-for-production-at-least-32-bytes";

    private final JwtService jwtService = new JwtService(new JwtProperties(SECRET, Duration.ofMinutes(15)));

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

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(PropertiesOnly.class);

    @EnableConfigurationProperties(JwtProperties.class)
    static class PropertiesOnly {
    }

    @Test
    void properties_secretShorterThan32_contextFailsToStart() {
        contextRunner.withPropertyValues("security.jwt.secret=short")
                .run(context -> assertThat(context).hasFailed()
                        .getFailure().rootCause().hasMessageContaining("секрет короче 32 символов"));
    }

    @Test
    void properties_secretExactly32_contextStarts() {
        contextRunner.withPropertyValues("security.jwt.secret=" + "a".repeat(32), "security.jwt.lifetime=15m")
                .run(context -> assertThat(context).hasNotFailed()
                        .getBean(JwtProperties.class).extracting(JwtProperties::secret)
                        .isEqualTo("a".repeat(32)));
    }
}