package com.example.TaskTracker;

import com.example.TaskTracker.Enums.TaskPriority;
import com.example.TaskTracker.Enums.TaskStatus;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public class Task {
    private static final AtomicLong ID_COUNTER = new AtomicLong(0);
    private final long id;
    private final String title;
    private String description;
    private TaskPriority priority;
    private TaskStatus status;
    private Set<String> tags;
    public Task() {
        description = " ";
        id = ID_COUNTER.incrementAndGet();
        priority = TaskPriority.LOW;
        status = TaskStatus.NEW;
        tags = new HashSet<>();
        title = " ";
    }

    public Task(long id, String title, String description,
                TaskPriority priority, TaskStatus status, Set<String> tags) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.status = status;
        this.tags = tags;
    }
    public Task(String title, TaskPriority priority, String description) {
        this.title = title;
        this.priority = priority;
        this.description = description;
        this.id = ID_COUNTER.incrementAndGet();
        this.tags = new HashSet<>();
        this.status = TaskStatus.NEW;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }
    public void addTag(String tag){
        tags.add(tag);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id && Objects.equals(title, task.title) && Objects.equals(description, task.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, description);
    }
}
