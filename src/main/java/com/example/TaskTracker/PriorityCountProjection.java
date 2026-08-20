package com.example.TaskTracker;

import com.example.TaskTracker.Enums.TaskPriority;

public interface PriorityCountProjection {
    TaskPriority getPriority();
    long getCount();
}
