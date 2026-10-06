package com.example.TaskTracker.controller;

import com.example.TaskTracker.dto.PageResponse;
import com.example.TaskTracker.dto.TaskResponse;
import com.example.TaskTracker.model.TaskPriority;
import com.example.TaskTracker.model.TaskStatus;
import com.example.TaskTracker.service.TaskService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Операции администратора. Цепочка фильтров пускает сюда любого вошедшего,
 * а роль проверяет аннотация на методе — так снятие @PreAuthorize сразу видно по тестам.
 */
@RestController
@RequestMapping("/api/admin/tasks")
public class AdminTaskController {

    private final TaskService taskService;

    public AdminTaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<TaskResponse>> allTasks(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String tag,
            Pageable pageable) {
        return ResponseEntity.ok(new PageResponse<>(
                taskService.searchAll(status, priority, title, tag, pageable)));
    }
}
