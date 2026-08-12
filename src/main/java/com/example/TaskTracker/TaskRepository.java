package com.example.TaskTracker;

import com.example.TaskTracker.Enums.TaskPriority;
import com.example.TaskTracker.Enums.TaskStatus;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Repository
public class TaskRepository {
    private final Map<Long, Task> tasks = new HashMap<>();
    public List<Task> getAllTasks(){
        return new ArrayList<>(tasks.values());
    }
    public Task getTaskById(long id){
        return tasks.get(id);
    }
    public Task createTask(String title, TaskPriority priority, String description){
        Task newTask = new Task(title, priority, description);
        tasks.put(newTask.getId(), newTask);
        return newTask;
    }
    public void changeStatus(TaskStatus status, long id){
        tasks.get(id).setStatus(status);
    }
    public void changePriority(TaskPriority priority, long id){
        tasks.get(id).setPriority(priority);
    }
    public void addTag(String tag, long id){
        tasks.get(id).addTag(tag);
    }
    public void deleteTask(long id){
        tasks.remove(id);
    }
    public void getStatistic(){
        TaskStatistic statistic = new TaskStatistic(new ArrayList<>(tasks.values()));
        System.out.println("Всего задач: " + statistic.getTotalTasks());
        System.out.println("По приоритету: " + statistic.getCountByPriority());
        System.out.println("По статусу: " + statistic.getCountByStatus());
    }
}
