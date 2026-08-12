package com.example.TaskTracker;

import com.example.TaskTracker.Enums.TaskPriority;
import com.example.TaskTracker.Enums.TaskStatus;
import com.example.TaskTracker.Exceptions.ChangeStatusException;
import com.example.TaskTracker.Exceptions.FoundTaskException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
@Service
public class TaskService {
    private final TaskRepository taskRepository;
    public TaskService(TaskRepository taskRepository){
        this.taskRepository = taskRepository;
    }
    private void validation(Task task, RuntimeException e){
        if(task == null)
            throw e;
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
    public List<Task> getAllTasks(){
        return taskRepository.getAllTasks();
    }
    public Task getTaskById(long id){
        validation(taskRepository.getTaskById(id), new FoundTaskException());
        return taskRepository.getTaskById(id);
    }
    public Task createTask(String title, TaskPriority priority, String description){
        return taskRepository.createTask(title, priority, description);
    }
    public Task changeStatus(TaskStatus status, long id){
        validation(getTaskById(id), new FoundTaskException());
        Task newTask = getTaskById(id);
        newTask.setStatus(status);
        if(switchStatus(status, getTaskById(id).getStatus()))
            taskRepository.changeStatus(status, id);
        else
            throw new ChangeStatusException();
        return newTask;
    }
    public Task changePriority(TaskPriority priority, long id){
        validation(getTaskById(id), new FoundTaskException());
        Task newTask = getTaskById(id);
        taskRepository.changePriority(priority, id);
        return newTask;
    }
    public List<String> addTag(String tag, long id){
        validation(getTaskById(id), new FoundTaskException());
        taskRepository.addTag(tag, id);
        return new ArrayList<>(getTaskById(id).getTags());
    }
    public void deleteTask(long id){
        validation(getTaskById(id), new FoundTaskException());
        taskRepository.deleteTask(id);
    }
    public void getStatistic(){
        taskRepository.getStatistic();
    }

}
