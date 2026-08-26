package com.example.TaskTracker.integration;

import com.example.TaskTracker.dto.TaskResponse;
import com.example.TaskTracker.exception.ExistingTagException;
import com.example.TaskTracker.model.TaskPriority;
import com.example.TaskTracker.service.TaskService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionRollbackTest extends AbstractIntegrationTest {

    @Autowired
    private TaskService taskService;
    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    @DisplayName("Операция из двух шагов падает на втором: в базе не осталось ни задачи, ни тегов")
    void twoSteps_failOnSecond_leavesNothingInDatabase(){

        assertThatThrownBy(() ->
                transactionTemplate.executeWithoutResult(status -> {
                    TaskResponse task = taskService.createTask(
                            "Откат", TaskPriority.HIGH, "две операции");
                    taskService.addTag("dup", task.getId());
                    taskService.addTag("dup", task.getId());
                })
        ).isInstanceOf(ExistingTagException.class);

        Long tasks = jdbcTemplate.queryForObject(
                "select count(*) from tasks", Long.class);
        Long tags = jdbcTemplate.queryForObject(
                "select count(*) from task_tags", Long.class);

        assertThat(tasks).isZero();
        assertThat(tags).isZero();
    }

    @Autowired
    private FailingTaskOperations failingTaskOperations;

    @Test
    @DisplayName("Метод из двух шагов падает на втором: ни задачи, ни тегов в базе")
    void createTaskWithTagsThenFail_failsAfterWrites_leavesNothingInDatabase(){

        assertThatThrownBy(() -> failingTaskOperations.createTaskWithTagsThenFail(
                "Откат", TaskPriority.HIGH, "две операции"))
                .isInstanceOf(IllegalStateException.class);

        Long tasks = jdbcTemplate.queryForObject("select count(*) from tasks", Long.class);
        Long tags = jdbcTemplate.queryForObject("select count(*) from task_tags", Long.class);

        assertThat(tasks).isZero();
        assertThat(tags).isZero();
    }
}
