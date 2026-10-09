package com.example.TaskTracker.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
@Service
public class JwtService {
    public static final Duration TOKEN_LIFETIME = Duration.ofMinutes(15);
    /** HS256 требует ключ не короче 256 бит. */
    public static final int MIN_SECRET_BYTES = 32;
    private final JwtEncoder encoder;
    public JwtService(@Value("${security.jwt.secret}") String secret){
        // Проверка при старте: иначе приложение поднималось с коротким секретом,
        // а каждый вход падал с 500 (KeyLengthException внутри Nimbus).
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < MIN_SECRET_BYTES)
            throw new IllegalStateException("JWT_SECRET слишком короткий: " + secretBytes.length
                    + " байт, нужно не меньше " + MIN_SECRET_BYTES);
        SecretKeySpec key = new SecretKeySpec(secretBytes, "HmacSHA256");
        this.encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
    }

    public String issue(String username, List<String> roles){
        Instant now = Instant.now();
        JwtClaimsSet claim = JwtClaimsSet.builder()
                .issuer("task-tracker")
                .issuedAt(now)
                .expiresAt(now.plus(TOKEN_LIFETIME))
                .subject(username)
                .claim("roles", roles)
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claim)).getTokenValue();
    }
}
