package com.example.TaskTracker.service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import com.example.TaskTracker.model.Task;
import com.example.TaskTracker.model.TaskPriority;
import com.example.TaskTracker.model.TaskStatus;
import com.example.TaskTracker.exception.ChangeStatusException;
import com.example.TaskTracker.exception.FoundTaskException;
import com.example.TaskTracker.repository.TaskRepository;
import com.example.TaskTracker.dto.TaskResponse;
import com.example.TaskTracker.dto.TaskStatistic;
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
    @Transactional(readOnly = true)
    public Page<TaskResponse> search(TaskStatus status, TaskPriority priority,
                                     String title, Pageable pageable){
        return taskRepository.search(status, priority, title, pageable).map(TaskResponse::new);
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
    public TaskResponse changeStatus(TaskStatus status, long id){
        Task newTask = findTaskEntity(id);
        if(newTask.getStatus().canMoveTo(status))
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
        return new TaskStatistic(
                taskRepository.countGroupedByStatus(),
                taskRepository.countGroupedByPriority());
    }
}
