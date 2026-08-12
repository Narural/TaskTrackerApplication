package com.example.TaskTracker;

public class ChangeStatusRequest {
    @ValidTaskStatus
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
