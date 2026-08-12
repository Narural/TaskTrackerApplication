package com.example.TaskTracker;

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
