package com.example.TaskTracker.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@ConfigurationProperties(prefix = "security.jwt")
@Validated
public record JwtProperties(
        @NotBlank(message = "секрет не задан")
        @Size(min = 32, message = "секрет короче 32 символов")
        String secret,
        @NotNull(message = "срок жизни токена не задан")
        Duration lifetime) {
}