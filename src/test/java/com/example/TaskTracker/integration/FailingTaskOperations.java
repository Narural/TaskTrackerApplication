package com.example.TaskTracker.integration;

import com.example.TaskTracker.model.AppUser;
import com.example.TaskTracker.model.Task;
import com.example.TaskTracker.model.TaskPriority;
import com.example.TaskTracker.repository.AppUserRepository;
import com.example.TaskTracker.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Операция из двух шагов, падающая на втором.
 * Лежит в тестовых исходниках и в собранный jar не попадает — это замена
 * удалённому отладочному эндпоинту test-rollback.
 */
@Service
public class FailingTaskOperations {

    private final TaskRepository taskRepository;
    private final AppUserRepository userRepository;

    public FailingTaskOperations(TaskRepository taskRepository, AppUserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void createTaskWithTagsThenFail(String title, TaskPriority priority, String description,
                                           String username) {
        AppUser owner = userRepository.findByUsername(username).orElseThrow();
        Task task = taskRepository.save(new Task(title, priority, description, owner)); // шаг 1: запись
        task.addTag("first");                                                            // шаг 2: теги
        task.addTag("second");
        throw new IllegalStateException("падение после записи задачи и тегов");
    }
}
