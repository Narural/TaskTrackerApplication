package com.example.TaskTracker.controller;

import com.example.TaskTracker.dto.AddTagRequest;
import com.example.TaskTracker.dto.ChangePriorityRequest;
import com.example.TaskTracker.dto.ChangeStatusRequest;
import com.example.TaskTracker.dto.CreateTaskRequest;
import com.example.TaskTracker.dto.PageResponse;
import com.example.TaskTracker.dto.TaskResponse;
import com.example.TaskTracker.dto.TaskStatistic;
import com.example.TaskTracker.model.TaskPriority;
import com.example.TaskTracker.model.TaskStatus;
import com.example.TaskTracker.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * Список задач с необязательными фильтрами.
     * Раньше на это было три отдельных адреса, а неизвестный параметр молча игнорировался.
     */
    @GetMapping
    public ResponseEntity<PageResponse<TaskResponse>> getTasks(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) String title,
            Pageable pageable) {
        return ResponseEntity.ok(
                new PageResponse<>(taskService.search(status, priority, title, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable long id) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody CreateTaskRequest request) {
        TaskResponse newTask = taskService.createTask(
                request.getTitle(), request.getPriority(), request.getDescription());
        URI location = URI.create("/api/tasks/" + newTask.getId());
        return ResponseEntity.created(location).body(newTask);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TaskResponse> changeStatus(@Valid @RequestBody ChangeStatusRequest request,
                                                     @PathVariable long id) {
        return ResponseEntity.ok(taskService.changeStatus(TaskStatus.valueOf(request.getStatus()), id));
    }

    @PatchMapping("/{id}/priority")
    public ResponseEntity<TaskResponse> changePriority(@Valid @RequestBody ChangePriorityRequest request,
                                                       @PathVariable long id) {
        return ResponseEntity.ok(taskService.changePriority(TaskPriority.valueOf(request.getPriority()), id));
    }

    @PostMapping("/{id}/tags")
    public ResponseEntity<List<String>> addTag(@Valid @RequestBody AddTagRequest tagRequest,
                                               @PathVariable long id) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.addTag(tagRequest.getTag(), id));
    }

    @DeleteMapping("/{id}/tags/{tag}")
    public ResponseEntity<Void> removeTag(@PathVariable long id, @PathVariable String tag) {
        taskService.removeTag(tag, id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/statistic")
    public ResponseEntity<TaskStatistic> showStatistic() {
        return ResponseEntity.ok(taskService.getStatistic());
    }
}
