package com.example.TaskTracker.dto;

import com.example.TaskTracker.model.Task;
import com.example.TaskTracker.model.TaskPriority;
import com.example.TaskTracker.model.TaskStatus;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class TaskResponse {
    private final Long id;
    private final String title;
    private final String description;
    private final TaskPriority priority;
    private final TaskStatus status;
    private final LocalDateTime createdAt;
    private final Set<String> tags;
    private final String owner;

    public TaskResponse(Task task) {
        this.id = task.getId();
        this.title = task.getTitle();
        this.description = task.getDescription();
        this.priority = task.getPriority();
        this.status = task.getStatus();
        this.createdAt = task.getCreatedAt();
        this.tags = new HashSet<>(task.getTags());
        this.owner = task.getOwner() != null ? task.getOwner().getUsername() : null;
    }

    public String getOwner() {
        return owner;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Set<String> getTags() {
        return tags;
    }
}
