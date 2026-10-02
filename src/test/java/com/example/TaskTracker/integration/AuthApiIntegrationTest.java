package com.example.TaskTracker.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AuthApiIntegrationTest extends AbstractIntegrationTest {

    @Test
    void register_newUser_returns201WithoutPassword() {
        rest.post().uri("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("username", "masha", "password", "secret123"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.username").isEqualTo("masha")
                .jsonPath("$.role").isEqualTo("USER")
                .jsonPath("$.password").doesNotExist()
                .jsonPath("$.passwordHash").doesNotExist();
    }

    @Test
    void register_newUser_storesHashNotPassword() {
        rest.post().uri("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("username", "masha", "password", "secret123"))
                .exchange()
                .expectStatus().isCreated();

        String hash = jdbcTemplate.queryForObject(
                "select password_hash from users where username = ?", String.class, "masha");

        assertThat(hash).isNotEqualTo("secret123");
        assertThat(passwordEncoder.matches("secret123", hash)).isTrue();
    }

    @Test
    void  register_duplicateUsername_returns409AndKeepsOneRowInDb(){
        rest.post().uri("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("username", "masha", "password", "secret123"))
                .exchange()
                .expectStatus().isCreated();
        rest.post().uri("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("username", "masha", "password", "secret123"))
                .exchange()
                .expectStatus().isEqualTo(409);
        String username = "masha";
        Long usersCount = jdbcTemplate.queryForObject("select count(*) from users where username = ?", Long.class, username);
        assertThat(usersCount).isEqualTo(1);
    }

    @Test
    void register_shortPasswordAndLong_registerNoUsersAndReturns400(){
        rest.post().uri("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("username", "masha", "password", String.valueOf("a").repeat(7)))
                .exchange()
                .expectStatus().isEqualTo(400);
        rest.post().uri("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("username", "masha", "password", String.valueOf("a").repeat(71)))
                .exchange()
                .expectStatus().isEqualTo(400);
        String username = "masha";
        Long usersCount = jdbcTemplate.queryForObject("select count(*) from users where username = ?", Long.class, username);
        assertThat(usersCount).isEqualTo(0);
    }

    @Test
    void login_unknownUser_returns401() {
        anonymous.post().uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("username", "ghost", "password", "whatever123"))
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.code").isEqualTo("BAD_CREDENTIALS")
                .jsonPath("$.message").isEqualTo("Неверное имя пользователя или пароль");
    }

    @Test
    void login_existingUserWrongPassword_returnsSame401AsUnknownUser() {
        anonymous.post().uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("username", USER, "password", "wrongpass123"))
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.code").isEqualTo("BAD_CREDENTIALS")
                .jsonPath("$.message").isEqualTo("Неверное имя пользователя или пароль");
    }

    @Test
    void login_validUser_tokenContainsOnlyRolesWithoutPrefix() {
        Map<?, ?> body = rest.post().uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("username", USER, "password", USER_PASS))
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .returnResult().getResponseBody();

        String token = (String) body.get("token");
        String payload = new String(
                Base64.getUrlDecoder().decode(token.split("\\.")[1]), StandardCharsets.UTF_8);

        assertThat(body.get("expiresIn")).isEqualTo(900);
        assertThat(payload).contains("\"sub\":\"" + USER + "\"");
        assertThat(payload).contains("\"roles\":[\"USER\"]");
    }
}