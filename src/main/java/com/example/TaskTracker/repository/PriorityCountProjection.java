package com.example.TaskTracker.repository;

import com.example.TaskTracker.model.TaskPriority;

public interface PriorityCountProjection {
    TaskPriority getPriority();
    long getCount();
}
