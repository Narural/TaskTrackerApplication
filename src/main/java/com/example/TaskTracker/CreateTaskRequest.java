package com.example.TaskTracker;

import com.example.TaskTracker.Enums.TaskPriority;
import com.example.TaskTracker.Enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.apache.logging.log4j.message.Message;

public class CreateTaskRequest {
    @NotBlank(message = "Название не может быть пустым")
    @Size(max = 100, message = "Размер названия не должен превышать 100 символов")
    String title;
    @NotNull(message = "Приоритет не может быть пустым")
    TaskPriority priority;
    @NotBlank(message = "Описание не может быть пустым")
    @Size(max = 100, message = "Размер описания не должен превышать 100 символов")
    String description;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
