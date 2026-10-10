package com.example.TaskTracker.ops;

import com.example.TaskTracker.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

class ActuatorAccessIntegrationTest extends AbstractIntegrationTest {

    @Test
    void health_anonymous_returns200WithoutComponents() {
        anonymous.get().uri("/actuator/health").exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("UP")
                .jsonPath("$.components").doesNotExist();
    }

    @Test
    void info_anonymous_returns401() {
        anonymous.get().uri("/actuator/info").exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void info_userRole_returns403() {
        rest.get().uri("/actuator/info").exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void info_adminRole_returns200() {
        anonymous.get().uri("/actuator/info")
                .headers(h -> h.setBearerAuth(tokenFor(ADMIN, ADMIN_PASS)))
                .exchange()
                .expectStatus().isOk();
    }
}