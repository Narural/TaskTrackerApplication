package com.example.TaskTracker.dto;

import com.example.TaskTracker.validation.ValidTaskPriority;

public class ChangePriorityRequest {
    @ValidTaskPriority
    private String priority;

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }
}
