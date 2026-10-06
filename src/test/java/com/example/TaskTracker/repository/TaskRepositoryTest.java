package com.example.TaskTracker.repository;

import com.example.TaskTracker.model.AppUser;
import com.example.TaskTracker.model.Role;
import com.example.TaskTracker.model.Task;
import com.example.TaskTracker.model.TaskPriority;
import com.example.TaskTracker.model.TaskStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.jdbc.core.JdbcTemplate;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.Session;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
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
    private AppUserRepository userRepository;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${spring.jpa.hibernate.ddl-auto}")
    private String ddlAuto;

    private AppUser owner;
    private AppUser stranger;

    @BeforeEach
    void createOwners() {
        owner = userRepository.save(new AppUser("owner", "hash", Role.USER));
        stranger = userRepository.save(new AppUser("stranger", "hash", Role.USER));
    }

    private Task task(String title, TaskPriority priority, AppUser taskOwner) {
        return taskRepository.save(new Task(title, priority, "a", taskOwner));
    }

    @Test
    void findByTitleContainingIgnoreCase_diffCases_findTasks(){
        task("ТЕсты", TaskPriority.HIGH, owner);
        task("тесты", TaskPriority.HIGH, owner);
        var found = taskRepository.searchOwned("owner", null, null, "Тесты", null, PageRequest.of(0, 10));
        assertThat(found.getTotalElements()).isEqualTo(2);
        assertThat(found.getContent())
                .extracting(Task::getTitle)
                .containsExactlyInAnyOrder("ТЕсты", "тесты");
    }

    @Test
    void findByStatus_allowedStatus_findTasks(){
        task("ТЕсты", TaskPriority.HIGH, owner);
        task("тесты", TaskPriority.HIGH, owner);
        Task task3 = task("теfffсты", TaskPriority.HIGH, owner);
        ReflectionTestUtils.setField(task3, "status", TaskStatus.DONE);
        var found = taskRepository.searchOwned("owner", TaskStatus.NEW, null, null, null, PageRequest.of(0, 10));
        assertThat(found.getTotalElements()).isEqualTo(2);
        assertThat(found.getContent())
                .extracting(Task::getTitle)
                .containsExactlyInAnyOrder("ТЕсты", "тесты");
    }

    @Test
    @DisplayName("Список владельца не содержит чужих строк — ни в содержимом, ни в счётчике")
    void searchOwned_twoOwners_returnsOnlyOwnRows(){
        task("Моя", TaskPriority.HIGH, owner);
        task("Чужая", TaskPriority.HIGH, stranger);

        var found = taskRepository.searchOwned("owner", null, null, null, null, PageRequest.of(0, 10));

        assertThat(found.getContent()).extracting(Task::getTitle).containsExactly("Моя");
        assertThat(found.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Чужая задача по id не находится — так же, как несуществующая")
    void findByIdAndOwnerUsername_foreignTask_isEmpty(){
        Task foreign = task("Чужая", TaskPriority.HIGH, stranger);

        assertThat(taskRepository.findByIdAndOwnerUsername(foreign.getId(), "owner")).isEmpty();
        assertThat(taskRepository.findByIdAndOwnerUsername(foreign.getId(), "stranger")).isPresent();
    }

    @Test
    @DisplayName("Фильтр по тегу возвращает только задачи с этим тегом")
    void searchOwned_byTag_returnsOnlyTaggedTasks(){
        Task tagged = task("С тегом", TaskPriority.HIGH, owner);
        tagged.addTag("backend");
        task("Без тега", TaskPriority.HIGH, owner);
        taskRepository.flush();

        var found = taskRepository.searchOwned("owner", null, null, null, "backend", PageRequest.of(0, 10));

        assertThat(found.getContent()).extracting(Task::getTitle).containsExactly("С тегом");
        assertThat(found.getTotalElements()).isEqualTo(1);
    }

    @Test
    void deleteCascade_existingTaskDelete_taskTagsDeletesWithTask(){
        Task task1 = task("ТЕсты", TaskPriority.HIGH, owner);
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
        Task task1 = task("ТЕсты", TaskPriority.HIGH, owner);
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
            Task saved = task("Задача " + i, TaskPriority.LOW, owner);
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

        assertThat(applied).containsExactly("1", "2", "3", "4", "5");
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
        Map<TaskStatus, Long> counts = taskRepository.countGroupedByStatus("sql-owner").stream()
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
        Map<TaskPriority, Long> counts = taskRepository.countGroupedByPriority("sql-owner").stream()
                .collect(Collectors.toMap(PriorityCountProjection::getPriority, PriorityCountProjection::getCount));
        assertThat(counts)
                .containsEntry(TaskPriority.CRITICAL, 1L)
                .containsEntry(TaskPriority.HIGH, 2L)
                .containsEntry(TaskPriority.LOW, 1L)
                .containsEntry(TaskPriority.MEDIUM, 1L);
    }

    @Test
    @Sql("/sql/tasks-mixed.sql")
    @DisplayName("Статистика считает только задачи владельца: чужие пять задач не видны")
    void countGroupedByStatus_otherOwner_seesNothingOfForeignTasks(){
        assertThat(taskRepository.countGroupedByStatus("owner")).isEmpty();
    }
}
