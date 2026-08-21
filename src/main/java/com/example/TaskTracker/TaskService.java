package com.example.TaskTracker;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import com.example.TaskTracker.Enums.TaskPriority;
import com.example.TaskTracker.Enums.TaskStatus;
import com.example.TaskTracker.Exceptions.ChangeStatusException;
import com.example.TaskTracker.Exceptions.FoundTaskException;
import jdk.jshell.Snippet;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
@Service
public class TaskService {
    private final TaskRepository taskRepository;
    public TaskService(TaskRepository taskRepository){
        this.taskRepository = taskRepository;
    }
    private Task findTaskEntity(long id){
        return taskRepository.findById(id).orElseThrow(FoundTaskException::new);
    }
    private boolean switchStatus(TaskStatus newStatus, TaskStatus currentStatus){
        return switch (currentStatus){
            case NEW -> newStatus == TaskStatus.IN_PROCESS ||
                        newStatus == TaskStatus.CANCELED;
            case IN_PROCESS -> newStatus == TaskStatus.DONE || newStatus == TaskStatus.NEW
                                            || newStatus == TaskStatus.CANCELED;
            case DONE -> newStatus == TaskStatus.IN_PROCESS || newStatus == TaskStatus.CANCELED;
            default -> false;
        };
    }
    @Transactional(readOnly = true)
    public Page<TaskResponse> getAllTasks(Pageable pageable){
        return taskRepository.findAll(pageable).map(TaskResponse::new);
    }
    @Transactional(readOnly = true)
    public TaskResponse getTaskById(long id){
        return new TaskResponse(findTaskEntity(id));
    }
    @Transactional
    public TaskResponse createTask(String title, TaskPriority priority, String description){
        Task newTask = new Task(title, priority, description);
        return new TaskResponse(taskRepository.save(newTask));
    }
    @Transactional
    public void createTaskWithFail(String title, TaskPriority priority, String description){
        Task newTask = new Task(title, priority, description);
        taskRepository.save(newTask);
        newTask.addTag("rawe");
        newTask.addTag("aaa");
        throw new RuntimeException();
    }
    @Transactional
    public TaskResponse changeStatus(TaskStatus status, long id){
        Task newTask = findTaskEntity(id);
        if(switchStatus(status, newTask.getStatus()))
            newTask.setStatus(status);
        else
            throw new ChangeStatusException();
        return new TaskResponse(newTask);
    }
    @Transactional
    public TaskResponse changePriority(TaskPriority priority, long id){
        Task newTask = findTaskEntity(id);
        newTask.setPriority(priority);
        return new TaskResponse(newTask);
    }
    @Transactional
    public List<String> addTag(String tag, long id){
        Task newTask = findTaskEntity(id);
        newTask.addTag(tag);
        return new ArrayList<>(newTask.getTags());
    }
    @Transactional
    public List<String> removeTag(String tag, long id){
        Task newTask = findTaskEntity(id);
        newTask.removeTag(tag);
        return new ArrayList<>(newTask.getTags());
    }
    @Transactional
    public void deleteTask(long id){
        Task newTask = findTaskEntity(id);
        taskRepository.delete(newTask);
    }
    @Transactional(readOnly = true)
    public TaskStatistic getStatistic(){
        return new TaskStatistic(taskRepository.count(),
                taskRepository.countGroupedByStatus(),
                taskRepository.countGroupedByPriority());
    }
    @Transactional(readOnly = true)
    public Page<TaskResponse> findByStatus(TaskStatus status, Pageable pageable){
        return taskRepository.findByStatus(status, pageable).map(TaskResponse::new);
    }
    @Transactional(readOnly = true)
    public Page<TaskResponse> findByStatusAndPriority(TaskStatus status,
                                                      TaskPriority priority,
                                                      Pageable pageable){
        return taskRepository.findByStatusAndPriority
                (status, priority, pageable).map(TaskResponse::new);
    }
    @Transactional(readOnly = true)
    public Page<TaskResponse> findByTitleContainingIgnoreCase(String title,
                                                              Pageable pageable){
        return taskRepository.findByTitleContainingIgnoreCase
                (title, pageable).map(TaskResponse::new);
    }

}
