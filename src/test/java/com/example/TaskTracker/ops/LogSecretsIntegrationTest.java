package com.example.TaskTracker.ops;

import com.example.TaskTracker.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(OutputCaptureExtension.class)
class LogSecretsIntegrationTest extends AbstractIntegrationTest {

    @Test
    void registerLoginAndRequest_logHasNoPasswordAndNoToken(CapturedOutput output) {
        String password = "very-secret-pass-42";

        anonymous.post().uri("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("username", "logcheck", "password", password))
                .exchange()
                .expectStatus().isCreated();
        String token = tokenFor("logcheck", password);
        anonymous.get().uri("/api/tasks")
                .headers(h -> h.setBearerAuth(token))
                .exchange()
                .expectStatus().isOk();

        assertThat(output).contains("Зарегистрирован пользователь logcheck");
        assertThat(output)
                .doesNotContain(password)
                .doesNotContain(token)
                .doesNotContain("eyJ");
    }
}