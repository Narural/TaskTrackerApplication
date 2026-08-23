package com.example.TaskTracker.controller;

import com.example.TaskTracker.model.TaskPriority;
import com.example.TaskTracker.model.TaskStatus;
import com.example.TaskTracker.dto.ChangePriorityRequest;
import com.example.TaskTracker.dto.ChangeStatusRequest;
import com.example.TaskTracker.dto.CreateTaskRequest;
import com.example.TaskTracker.dto.TaskResponse;
import com.example.TaskTracker.dto.TaskStatistic;
import com.example.TaskTracker.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import com.example.TaskTracker.dto.AddTagRequest;
@RestController
@RequestMapping("/api/task")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }
    @GetMapping
    public ResponseEntity<Page<TaskResponse>> getAllTasks(Pageable pageable){
        return ResponseEntity.ok(taskService.getAllTasks(pageable));
    }
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable long id){
        return ResponseEntity.ok(taskService.getTaskById(id));
    }
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody
                                                   CreateTaskRequest request){
        TaskResponse newTask = taskService.createTask(request.getTitle(), request.getPriority(), request.getDescription());
        URI location = URI.create("/api/task/" + newTask.getId());
        return ResponseEntity.created(location).body(newTask);
    }
    @PatchMapping("/{id}/status")
    public ResponseEntity<TaskResponse> changeStatus(@Valid @RequestBody
                                                         ChangeStatusRequest request,
                                             @PathVariable long id){
        return ResponseEntity.ok(taskService.changeStatus(TaskStatus.valueOf(request.getStatus()), id));
    }
    @PatchMapping("/{id}/priority")
    public ResponseEntity<TaskResponse> changePriority(@Valid @RequestBody
                                                           ChangePriorityRequest request,
                                               @PathVariable long id){
        return ResponseEntity.ok(taskService.changePriority(TaskPriority.valueOf(request.getPriority()), id));
    }
    @PostMapping("/{id}/tags")
    public ResponseEntity<List<String>> addTag(@Valid @RequestBody AddTagRequest tagRequest, @PathVariable long id){
        return ResponseEntity.status(HttpStatus.CREATED).
                body(taskService.addTag(tagRequest.getTag(), id));
    }
    @DeleteMapping("/{id}/tags/{tag}")
    public ResponseEntity<List<String>> removeTag(@PathVariable long id, @PathVariable String tag){

        return ResponseEntity.ok(taskService.removeTag(tag, id));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable long id){
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/statistic")
    public ResponseEntity<TaskStatistic> showStatistic() {
        return ResponseEntity.ok(taskService.getStatistic());
    }
    @GetMapping("/byStatus")
    public ResponseEntity<Page<TaskResponse>> findByStatus(
             @RequestParam TaskStatus status, Pageable pageable){
        return ResponseEntity.ok(taskService.findByStatus(status, pageable));
    }
    @GetMapping("/byStatusAndPriority")
    public ResponseEntity<Page<TaskResponse>> findByStatus(
                                                       @RequestParam TaskStatus status,
                                                   @RequestParam TaskPriority priority,
                                                           Pageable pageable){
        return ResponseEntity.ok(
                taskService.findByStatusAndPriority(status, priority, pageable));
    }
    @GetMapping("/byTitle")
    public ResponseEntity<Page<TaskResponse>> findByTitle(@Valid String title,
                                                          Pageable pageable){
        return ResponseEntity.ok(taskService.findByTitleContainingIgnoreCase(title, pageable));
    }
    @PostMapping("/test-rollback")
    public void testRollback() {

        taskService.createTaskWithFail(
                "Rollback task",
                TaskPriority.HIGH,
                "Testing transaction"
        );
    }
}
