package com.example.TaskTracker;

import com.example.TaskTracker.Enums.TaskPriority;
import com.example.TaskTracker.Enums.TaskStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/task")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }
    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks(){
        return ResponseEntity.ok(taskService.getAllTasks());
    }
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable long id){
        return ResponseEntity.ok(taskService.getTaskById(id));
    }
    @PostMapping
    public ResponseEntity<Task> createTask(@Valid @RequestBody
                                               CreateTaskRequest request){
        Task newTask = taskService.createTask(request.getTitle(), request.getStatus(), request.getDescription());
        URI location = URI.create("/api/task/" + newTask.getId());
        return ResponseEntity.created(location).body(newTask);
    }
    @PatchMapping("/{id}/status")
    public ResponseEntity<Task> changeStatus(@Valid @RequestBody
                                                 ChangeStatusRequest request,
                                             @PathVariable long id){
        return ResponseEntity.ok(taskService.changeStatus(TaskStatus.valueOf(request.getStatus()), id));
    }
    @PatchMapping("/{id}/priority")
    public ResponseEntity<Task> changePriority(@Valid @RequestBody
                                                   ChangePriorityRequest request,
                                               @PathVariable long id){
        return ResponseEntity.ok(taskService.changePriority(TaskPriority.valueOf(request.getPriority()), id));
    }
    @PostMapping("/{id}/tags/")
    public ResponseEntity<List<String>> addTag(@Valid @NotBlank String tag, @PathVariable long id){
        return ResponseEntity.ok(taskService.addTag(tag, id));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Task> deleteTask(@PathVariable long id){
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/statistic")
    public ResponseEntity<Void> showStatistic() {
        taskService.getStatistic();
        return ResponseEntity.ok().build();
    }


}
