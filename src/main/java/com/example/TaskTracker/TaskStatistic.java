package com.example.TaskTracker;

import com.example.TaskTracker.Enums.TaskPriority;
import com.example.TaskTracker.Enums.TaskStatus;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class TaskStatistic {
    private final int totalTasks;
    private final Map<TaskPriority, Integer> countByPriority;
    private final Map<TaskStatus, Integer> countByStatus;

    public TaskStatistic(List<Task> tasks) {
        this.totalTasks = tasks.size();
        this.countByPriority = new EnumMap<>(TaskPriority.class);
        this.countByStatus = new EnumMap<>(TaskStatus.class);
        for(Task task: tasks){
            countByStatus.merge(task.getStatus(), 1, Integer::sum);
            countByPriority.merge(task.getPriority(), 1, Integer::sum);
        }
    }

    public int getTotalTasks() {
        return totalTasks;
    }

    public Map<TaskPriority, Integer> getCountByPriority() {
        return countByPriority;
    }

    public Map<TaskStatus, Integer> getCountByStatus() {
        return countByStatus;
    }
}
