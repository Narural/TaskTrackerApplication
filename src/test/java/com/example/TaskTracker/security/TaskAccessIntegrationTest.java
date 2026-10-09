package com.example.TaskTracker.security;

import com.example.TaskTracker.integration.AbstractIntegrationTest;
import com.example.TaskTracker.model.AppUser;
import com.example.TaskTracker.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Главная проверка недели: два пользователя, у каждого по задаче, и попытки из-под одного
 * добраться до данных другого. Каждый отрицательный тест проверяет не только код ответа,
 * но и то, что в базе ничего не изменилось.
 *
 * rest — клиент пользователя USER (настроен в AbstractIntegrationTest), bob — второй пользователь.
 */
class TaskAccessIntegrationTest extends AbstractIntegrationTest {

    private static final String BOB = "bob";
    private static final String BOB_PASS = "bobpass123";

    private RestTestClient bob;

    @BeforeEach
    void createSecondUser() {
        userRepository.save(new AppUser(BOB, passwordEncoder.encode(BOB_PASS), Role.USER));
        String bobToken = tokenFor(BOB, BOB_PASS);
        bob = anonymous.mutate()
                .defaultHeaders(h -> h.setBearerAuth(bobToken))
                .build();
    }

    // ---------- список и чтение ----------

    @Test
    @DisplayName("Каждый видит в списке только свои задачи — и в содержимом, и в счётчике")
    void list_twoOwners_eachSeesOnlyOwnTasks() {
        createTask(rest, "Задача пользователя");
        createTask(bob, "Задача Боба");

        rest.get().uri("/api/tasks").exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.totalElements").isEqualTo(1)
                .jsonPath("$.content[0].title").isEqualTo("Задача пользователя");

        bob.get().uri("/api/tasks").exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.totalElements").isEqualTo(1)
                .jsonPath("$.content[0].title").isEqualTo("Задача Боба");
    }

    @Test
    @DisplayName("Фильтр по тегу не выдаёт чужие задачи с тем же тегом")
    void filterByTag_foreignTaskWithSameTag_isNotReturned() {
        long mine = createTask(rest, "Моя");
        long bobs = createTask(bob, "Боба");
        addTag(rest, mine, "shared");
        addTag(bob, bobs, "shared");

        rest.get().uri("/api/tasks?tag=shared").exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.totalElements").isEqualTo(1)
                .jsonPath("$.content[0].title").isEqualTo("Моя");
    }

    @Test
    @DisplayName("Чужая задача по прямому id даёт 404, как несуществующая, и остаётся в базе")
    void getTask_foreignOwner_returns404AndTaskStillExists() {
        long bobs = createTask(bob, "Задача Боба");

        rest.get().uri("/api/tasks/{id}", bobs).exchange()
                .expectStatus().isNotFound();

        assertThat(countTasks(bobs)).isEqualTo(1);
    }

    @Test
    @DisplayName("Свою задачу владелец читает и удаляет — пара к отрицательным тестам")
    void ownTask_owner_canReadAndDelete() {
        long mine = createTask(rest, "Моя");

        rest.get().uri("/api/tasks/{id}", mine).exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.owner").isEqualTo(USER);
        rest.delete().uri("/api/tasks/{id}", mine).exchange()
                .expectStatus().isNoContent();

        assertThat(countTasks(mine)).isZero();
    }

    // ---------- изменение чужого ----------

    @Test
    void changeStatus_foreignTask_returns404AndStatusUnchanged() {
        long bobs = createTask(bob, "Задача Боба");

        rest.patch().uri("/api/tasks/{id}/status", bobs)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("status", "IN_PROCESS"))
                .exchange()
                .expectStatus().isNotFound();

        assertThat(column(bobs, "status")).isEqualTo("NEW");
    }

    @Test
    void changePriority_foreignTask_returns404AndPriorityUnchanged() {
        long bobs = createTask(bob, "Задача Боба");

        rest.patch().uri("/api/tasks/{id}/priority", bobs)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("priority", "CRITICAL"))
                .exchange()
                .expectStatus().isNotFound();

        assertThat(column(bobs, "priority")).isEqualTo("LOW");
    }

    @Test
    void addTag_foreignTask_returns404AndNoTagAdded() {
        long bobs = createTask(bob, "Задача Боба");

        rest.post().uri("/api/tasks/{id}/tags", bobs)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("tag", "hacked"))
                .exchange()
                .expectStatus().isNotFound();

        assertThat(countTags(bobs)).isZero();
    }

    @Test
    void removeTag_foreignTask_returns404AndTagRemains() {
        long bobs = createTask(bob, "Задача Боба");
        addTag(bob, bobs, "important");

        rest.delete().uri("/api/tasks/{id}/tags/{tag}", bobs, "important")
                .exchange()
                .expectStatus().isNotFound();

        assertThat(countTags(bobs)).isEqualTo(1);
    }

    @Test
    void deleteTask_foreignTask_returns404AndTaskStillExists() {
        long bobs = createTask(bob, "Задача Боба");

        rest.delete().uri("/api/tasks/{id}", bobs)
                .exchange()
                .expectStatus().isNotFound();

        assertThat(countTasks(bobs)).isEqualTo(1);
    }

    // ---------- владелец берётся из токена ----------

    @Test
    @DisplayName("Владелец новой задачи — тот, кто пришёл с токеном, даже если в теле указан другой")
    void createTask_ownerInBody_isIgnored() {
        Long bobId = jdbcTemplate.queryForObject(
                "select id from users where username = ?", Long.class, BOB);

        Map<?, ?> created = rest.post().uri("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("title", "Подложить Бобу", "priority", "LOW", "description", "x",
                        "owner", BOB, "ownerId", bobId))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Map.class)
                .returnResult().getResponseBody();
        long id = ((Number) created.get("id")).longValue();

        String ownerInDb = jdbcTemplate.queryForObject(
                "select u.username from tasks t join users u on u.id = t.owner_id where t.id = ?",
                String.class, id);
        assertThat(ownerInDb).isEqualTo(USER);
    }

    @Test
    void createTask_withoutToken_returns401AndNothingCreated() {
        anonymous.post().uri("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("title", "Аноним", "priority", "LOW", "description", "x"))
                .exchange()
                .expectStatus().isUnauthorized();

        Long total = jdbcTemplate.queryForObject("select count(*) from tasks", Long.class);
        assertThat(total).isZero();
    }

    @Test
    @DisplayName("Статистика не раскрывает, сколько задач у других")
    void statistic_countsOnlyOwnTasks() {
        createTask(rest, "Моя", "LOW");
        long bobs = createTask(bob, "Боба 1", "HIGH");
        createTask(bob, "Боба 2", "CRITICAL");
        bob.patch().uri("/api/tasks/{id}/status", bobs)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("status", "IN_PROCESS"))
                .exchange()
                .expectStatus().isOk();

        // totalTasks складывается из счётчиков по статусам, поэтому по нему одному не видно
        // утечки в подсчёте по приоритетам — проверяем обе разбивки по отдельности.
        // У Боба другие приоритеты и другой статус: любая чужая строка даст ненулевой счётчик.
        rest.get().uri("/api/tasks/statistic").exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.totalTasks").isEqualTo(1)
                .jsonPath("$.countByPriority.LOW").isEqualTo(1)
                .jsonPath("$.countByPriority.HIGH").isEqualTo(0)
                .jsonPath("$.countByPriority.CRITICAL").isEqualTo(0)
                .jsonPath("$.countByStatus.NEW").isEqualTo(1)
                .jsonPath("$.countByStatus.IN_PROCESS").isEqualTo(0);
    }

    // ---------- администратор ----------

    @Test
    void adminTasks_userRole_returns403() {
        rest.get().uri("/api/admin/tasks").exchange()
                .expectStatus().isForbidden()
                .expectBody().jsonPath("$.code").isEqualTo("ACCESS_DENIED");
    }

    @Test
    void adminTasks_withoutToken_returns401() {
        anonymous.get().uri("/api/admin/tasks").exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void adminTasks_adminRole_seesTasksOfAllUsers() {
        createTask(rest, "Задача пользователя");
        createTask(bob, "Задача Боба");

        anonymous.get().uri("/api/admin/tasks")
                .headers(h -> h.setBearerAuth(tokenFor(ADMIN, ADMIN_PASS)))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.totalElements").isEqualTo(2);
    }

    // ---------- кто я ----------

    @Test
    void me_returnsCurrentUserWithoutPassword() {
        rest.get().uri("/api/me").exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.username").isEqualTo(USER)
                .jsonPath("$.role").isEqualTo("USER")
                .jsonPath("$.password").doesNotExist()
                .jsonPath("$.passwordHash").doesNotExist();
    }

    @Test
    void me_withoutToken_returns401() {
        anonymous.get().uri("/api/me").exchange()
                .expectStatus().isUnauthorized();
    }

    // ---------- помощники ----------

    private long createTask(RestTestClient client, String title) {
        return createTask(client, title, "LOW");
    }

    private long createTask(RestTestClient client, String title, String priority) {
        Map<?, ?> created = client.post().uri("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("title", title, "priority", priority, "description", "x"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Map.class)
                .returnResult().getResponseBody();
        return ((Number) created.get("id")).longValue();
    }

    private void addTag(RestTestClient client, long taskId, String tag) {
        client.post().uri("/api/tasks/{id}/tags", taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("tag", tag))
                .exchange()
                .expectStatus().isCreated();
    }

    private long countTasks(long id) {
        return jdbcTemplate.queryForObject("select count(*) from tasks where id = ?", Long.class, id);
    }

    private long countTags(long taskId) {
        return jdbcTemplate.queryForObject(
                "select count(*) from task_tags where task_id = ?", Long.class, taskId);
    }

    private String column(long id, String column) {
        return jdbcTemplate.queryForObject(
                "select " + column + " from tasks where id = ?", String.class, id);
    }
}
