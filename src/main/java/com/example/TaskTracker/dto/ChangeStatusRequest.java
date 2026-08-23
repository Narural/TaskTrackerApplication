package com.example.TaskTracker.dto;

import com.example.TaskTracker.validation.ValidTaskStatus;

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
