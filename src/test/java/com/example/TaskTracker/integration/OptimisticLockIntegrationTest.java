package com.example.TaskTracker.integration;

import com.example.TaskTracker.model.AppUser;
import com.example.TaskTracker.model.Task;
import com.example.TaskTracker.model.TaskPriority;
import org.junit.jupiter.api.Test;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OptimisticLockIntegrationTest extends AbstractIntegrationTest {

    @Test
    void save_staleCopy_isRejectedAndFirstChangeSurvives() {
        AppUser owner = userRepository.findByUsername(USER).orElseThrow();
        long id = taskRepository.save(new Task("общая", TaskPriority.LOW, "x", owner)).getId();

        Task first = taskRepository.findById(id).orElseThrow();
        Task second = taskRepository.findById(id).orElseThrow();

        first.setPriority(TaskPriority.HIGH);
        taskRepository.save(first);

        second.setPriority(TaskPriority.CRITICAL);
        assertThatThrownBy(() -> taskRepository.save(second))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);

        Task stored = taskRepository.findById(id).orElseThrow();
        assertThat(stored.getPriority()).isEqualTo(TaskPriority.HIGH);
        assertThat(stored.getVersion()).isEqualTo(first.getVersion() + 1);
    }
}