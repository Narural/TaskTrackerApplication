package com.example.TaskTracker.repository;

import com.example.TaskTracker.model.TaskStatus;

public interface StatusCountProjection {
    TaskStatus getStatus();
    long getCount();
}
