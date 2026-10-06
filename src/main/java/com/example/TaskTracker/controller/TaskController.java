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
import java.security.Principal;
import java.util.List;

/**
 * Контроллер не принимает решений о доступе: он только передаёт сервису, кто пришёл.
 * Имя берётся из проверенного токена (Principal), а не из тела запроса.
 */
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<TaskResponse>> getTasks(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String tag,
            Pageable pageable,
            Principal principal) {
        return ResponseEntity.ok(new PageResponse<>(
                taskService.search(principal.getName(), status, priority, title, tag, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable long id, Principal principal) {
        return ResponseEntity.ok(taskService.getTaskById(id, principal.getName()));
    }

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody CreateTaskRequest request,
                                                   Principal principal) {
        TaskResponse newTask = taskService.createTask(
                request.getTitle(), request.getPriority(), request.getDescription(), principal.getName());
        URI location = URI.create("/api/tasks/" + newTask.getId());
        return ResponseEntity.created(location).body(newTask);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TaskResponse> changeStatus(@Valid @RequestBody ChangeStatusRequest request,
                                                     @PathVariable long id, Principal principal) {
        return ResponseEntity.ok(taskService.changeStatus(
                TaskStatus.valueOf(request.getStatus()), id, principal.getName()));
    }

    @PatchMapping("/{id}/priority")
    public ResponseEntity<TaskResponse> changePriority(@Valid @RequestBody ChangePriorityRequest request,
                                                       @PathVariable long id, Principal principal) {
        return ResponseEntity.ok(taskService.changePriority(
                TaskPriority.valueOf(request.getPriority()), id, principal.getName()));
    }

    @PostMapping("/{id}/tags")
    public ResponseEntity<List<String>> addTag(@Valid @RequestBody AddTagRequest tagRequest,
                                               @PathVariable long id, Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskService.addTag(tagRequest.getTag(), id, principal.getName()));
    }

    @DeleteMapping("/{id}/tags/{tag}")
    public ResponseEntity<Void> removeTag(@PathVariable long id, @PathVariable String tag,
                                          Principal principal) {
        taskService.removeTag(tag, id, principal.getName());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable long id, Principal principal) {
        taskService.deleteTask(id, principal.getName());
        return ResponseEntity.noContent().build();
    }

    /** Статистика по своим задачам: считать чужие — значит раскрывать, сколько их у других. */
    @GetMapping("/statistic")
    public ResponseEntity<TaskStatistic> showStatistic(Principal principal) {
        return ResponseEntity.ok(taskService.getStatistic(principal.getName()));
    }
}
