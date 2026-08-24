package com.example.TaskTracker.repository;

import com.example.TaskTracker.model.Task;
import com.example.TaskTracker.model.TaskPriority;
import com.example.TaskTracker.model.TaskStatus;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class TaskRepositoryTest {
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private EntityManager entityManager;
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
    void countGroupedByStatus_filledStatuses_correctCount(){
        Task task1 = taskRepository.save(new Task("ТЕсты", TaskPriority.HIGH, "a"));
        Task task2 = taskRepository.save(new Task("тесты", TaskPriority.HIGH, "a"));
        var group = taskRepository.countGroupedByStatus();
        Map<TaskStatus, Long> counts = group.stream()
                .collect(Collectors.toMap(StatusCountProjection::getStatus,
                        StatusCountProjection::getCount));
        assertThat(counts).containsEntry(TaskStatus.NEW, 2L);
    }
    @Test
    void countGroupedByPriority_filledPriorities_correctCount(){
        Task task1 = taskRepository.save(new Task("ТЕсты", TaskPriority.HIGH, "a"));
        Task task2 = taskRepository.save(new Task("тесты", TaskPriority.LOW, "a"));
        var group = taskRepository.countGroupedByPriority();
        Map<TaskPriority, Long> counts = group.stream()
                .collect(Collectors.toMap(PriorityCountProjection::getPriority,
                        PriorityCountProjection::getCount));
        assertThat(counts).containsEntry(TaskPriority.HIGH, 1L);
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
                .isInstanceOf(DataIntegrityViolationException.class);
    }
    @Test
    void queryAmount_idkHowToNameThisTestNgl(){
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
}
