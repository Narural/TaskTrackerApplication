package com.example.TaskTracker.repository;

import com.example.TaskTracker.model.Task;
import com.example.TaskTracker.model.TaskPriority;
import com.example.TaskTracker.model.TaskStatus;
import jakarta.persistence.EntityManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.Session;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.junit.jupiter.api.DisplayName;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.springframework.test.context.jdbc.Sql;
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class    TaskRepositoryTest {
   @Container
    @ServiceConnection
     static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${spring.jpa.hibernate.ddl-auto}")
    private String ddlAuto;
    @Test
    void findByTitleContainingIgnoreCase_diffCases_findTasks(){
        taskRepository.save(new Task("ТЕсты", TaskPriority.HIGH, "a"));
        taskRepository.save(new Task("тесты", TaskPriority.HIGH, "a"));
        var found = taskRepository.findByTitleContainingIgnoreCase
                ("Тесты", PageRequest.of(0, 10));
        assertThat(found.getTotalElements()).isEqualTo(2);
        assertThat(found.getContent())
                .extracting(Task::getTitle)
                .containsExactlyInAnyOrder("ТЕсты", "тесты");
    }

    @Test
    void findByStatus_allowedStatus_findTasks(){
        Task task1 = taskRepository.save(new Task("ТЕсты", TaskPriority.HIGH, "a"));
        Task task2 = taskRepository.save(new Task("тесты", TaskPriority.HIGH, "a"));
        Task task3 = taskRepository.save(new Task("теfffсты", TaskPriority.HIGH, "a"));
        ReflectionTestUtils.setField(task3, "status", TaskStatus.DONE);
        var found = taskRepository.findByStatus
                (TaskStatus.NEW, PageRequest.of(0, 10));
        assertThat(found.getTotalElements()).isEqualTo(2);
        assertThat(found.getContent())
                .extracting(Task::getTitle)
                .containsExactlyInAnyOrder("ТЕсты", "тесты");
    }
    @Test
    void deleteCascade_existingTaskDelete_taskTagsDeletesWithTask(){
        Task task1 = taskRepository.save(new Task("ТЕсты", TaskPriority.HIGH, "a"));
        task1.addTag("a");
        taskRepository.flush();

        Long taskId = task1.getId();
        taskRepository.delete(task1);
        taskRepository.flush();

        Number count = (Number) entityManager
                .createNativeQuery("select count(*) from task_tags where task_id = ?1")
                .setParameter(1, taskId)
                .getSingleResult();

        assertThat(count.longValue()).isZero();
    }
    @Test
    void taskTags_duplicatePair_violatesUniqueConstraint(){
        Task task1 = taskRepository.save(new Task("ТЕсты", TaskPriority.HIGH, "a"));
        task1.addTag("a");
        taskRepository.flush();
        assertThatThrownBy(()->{
            entityManager
                .createNativeQuery("insert into task_tags (task_id, tag) values (?1, ?2)")
                .setParameter(1, task1.getId())
                .setParameter(2, "a")
                .executeUpdate();
            entityManager.flush();
        })
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("uq_task_tags");
    }
    @Test
    void getAllTasksWithTags_fourTasks_executesAtMostTwoQueries(){
        for (int i = 0; i < 3; i++) {
            Task saved = taskRepository.save(new Task("Задача " + i, TaskPriority.LOW, "a"));
            saved.addTag("tag" + i);
        }
        taskRepository.flush();
        entityManager.clear();

        Statistics statistics = entityManager.unwrap(Session.class)
                .getSessionFactory().getStatistics();
        statistics.clear();

        taskRepository.findAll(PageRequest.of(0, 10))
                .forEach(task -> task.getTags().size());

        assertThat(statistics.getPrepareStatementCount()).isLessThanOrEqualTo(2);
    }
    @Test
    @DisplayName("Все миграции применяются к пустой базе; схему с entity сверяет ddl-auto=validate")
    void flywayMigrations_emptyDatabase_allAppliedSuccessfully(){
        List<String> applied = jdbcTemplate.queryForList(
                "select version from flyway_schema_history where success = true order by installed_rank",
                String.class);

        Long failed = jdbcTemplate.queryForObject(
                "select count(*) from flyway_schema_history where success = false",
                Long.class);

        assertThat(applied).containsExactly("1", "2");
        assertThat(failed).isZero();
    }
    @Test
    @DisplayName("ddl-auto=validate включён — без него схема и entity не сверяются")
    void hibernateDdlAuto_inTests_isValidate(){
        assertThat(ddlAuto).isEqualTo("validate");
    }
    @Test
    @Sql("/sql/tasks-mixed.sql")
    @DisplayName("Счёт по статусам группирует все имеющиеся статусы")
    void countGroupedByStatus_mixedStates_countsEveryGroup(){
        Map<TaskStatus, Long> counts = taskRepository.countGroupedByStatus().stream()
                .collect(Collectors.toMap(StatusCountProjection::getStatus,
                        StatusCountProjection::getCount));

        assertThat(counts)
                .containsOnlyKeys(TaskStatus.NEW, TaskStatus.IN_PROCESS, TaskStatus.DONE)
                .containsEntry(TaskStatus.NEW, 2L)
                .containsEntry(TaskStatus.IN_PROCESS, 1L)
                .containsEntry(TaskStatus.DONE, 2L);
    }
    @Test
    @Sql("/sql/tasks-mixed.sql")
    @DisplayName("Счёт по приоритетам группирует имеющиеся статусы")
    void countGroupedBuPriority_mixedPriorities_countsEveryGroup(){
        Map<TaskPriority, Long> counts = taskRepository.countGroupedByPriority().stream()
                .collect(Collectors.toMap(PriorityCountProjection::getPriority, PriorityCountProjection::getCount));
        assertThat(counts)
                .containsEntry(TaskPriority.CRITICAL, 1L)
                .containsEntry(TaskPriority.HIGH, 2L)
                .containsEntry(TaskPriority.LOW, 1L)
                .containsEntry(TaskPriority.MEDIUM, 1L);
    }
}
