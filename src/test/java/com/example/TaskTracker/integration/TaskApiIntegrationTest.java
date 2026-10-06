package com.example.TaskTracker.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class TaskApiIntegrationTest extends AbstractIntegrationTest {
    @Autowired
    private JdbcClient jdbcClient;

    @Test
    @DisplayName("Полный путь: создать -> прочитать -> сменить статус -> увидеть в списке")
    void fullPath_createReadChangeStatus_showsNewStatus(){
        Map<String, Object> created= rest.post().uri("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("title", "Test",
                        "priority", "HIGH",
                        "description", "Test"))
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().exists("Location")
                .expectBody(Map.class)
                .returnResult().getResponseBody();

        long id = ((Number)created.get("id")).longValue();
        rest.get().uri("/api/tasks/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Test")
                .jsonPath("$.status").isEqualTo("NEW");
        rest.patch().uri("/api/tasks/{id}/status", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("status", "IN_PROCESS"))
                .exchange()
                .expectStatus().isOk();
        rest.get().uri("/api/tasks")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content[0].status").isEqualTo("IN_PROCESS");

        String statusInDb = jdbcTemplate.queryForObject("select status from tasks where id = ?", String.class, id);
        assertThat(statusInDb).isEqualTo("IN_PROCESS");
    }
    @Test
    @DisplayName("Повторный тэг выдаёт 409, в базе 1 строка")
    void addTag_addDuplicateTag_409InDbNoNewTag(){
        Map<String, Object> created= rest.post().uri("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("title", "Test",
                        "priority", "HIGH",
                        "description", "Test"))
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().exists("Location")
                .expectBody(Map.class)
                .returnResult().getResponseBody();
        long id = ((Number) created.get("id")).longValue();
        rest.post().uri("/api/tasks/{id}/tags", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("tag", "test"))
                .exchange()
                .expectStatus().isCreated();
        rest.post().uri("/api/tasks/{id}/tags", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("tag", "test"))
                .exchange()
                .expectStatus().isEqualTo(409);

        Long tagsFromTask = jdbcTemplate.queryForObject("select count(*) from task_tags where task_id = ?", Long.class, id);
        assertThat(tagsFromTask).isEqualTo(1);
    }
    @Test
    void deleteTask_deletesTagsToo_204NoTaskNoTags(){
        Map<String, Object> created= rest.post().uri("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("title", "Test",
                        "priority", "HIGH",
                        "description", "Test"))
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().exists("Location")
                .expectBody(Map.class)
                .returnResult().getResponseBody();
        long id = ((Number) created.get("id")).longValue();
        rest.post().uri("/api/tasks/{id}/tags", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("tag", "test"))
                .exchange()
                .expectStatus().isCreated();
        rest.delete().uri("/api/tasks/{id}", id).exchange()
                .expectStatus()
                .isNoContent();
        Long countTags = jdbcTemplate.queryForObject("select count(*) from task_tags where task_id = ?", Long.class, id);
        assertThat(countTags).isEqualTo(0);
        rest.get().uri("api/tasks/{id}", id)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("Статистика на пустой базе: все статусы присутствуют с нулями")
    void getStatistic_emptyDatabase_returnsAllStatusesWithZero(){
        rest.get().uri("/api/tasks/statistic")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.totalTasks").isEqualTo(0)
                .jsonPath("$.countByStatus.NEW").isEqualTo(0)
                .jsonPath("$.countByStatus.IN_PROCESS").isEqualTo(0)
                .jsonPath("$.countByStatus.DONE").isEqualTo(0)
                .jsonPath("$.countByStatus.CANCELED").isEqualTo(0)
                .jsonPath("$.countByPriority.LOW").isEqualTo(0);
    }

    @Test
    @DisplayName("Запрещённый переход даёт 400 и не меняет задачу в базе")
    void changeStatus_forbiddenTransition_returns400AndDatabaseUnchanged(){
        long id = createTask("Test", "HIGH", "Test");

        rest.patch().uri("/api/tasks/{id}/status", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("status", "DONE"))
                .exchange()
                .expectStatus().isBadRequest();

        String statusInDb = jdbcTemplate.queryForObject(
                "select status from tasks where id = ?", String.class, id);
        assertThat(statusInDb).isEqualTo("NEW");
    }

    @Test
    @DisplayName("Несуществующее поле сортировки даёт 400, а не 500")
    void getAllTasks_unknownSortProperty_returns400NotServerError(){
        rest.get().uri("/api/tasks?sort=nosuchfield,asc")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").exists();
    }

    private long createTask(String title, String priority, String description){
        Map<String, Object> created = rest.post().uri("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("title", title, "priority", priority, "description", description))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Map.class)
                .returnResult().getResponseBody();
        return ((Number) created.get("id")).longValue();
    }
}
