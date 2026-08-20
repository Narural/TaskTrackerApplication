package com.example.TaskTracker;

import com.example.TaskTracker.Enums.TaskStatus;

public interface StatusCountProjection {
    TaskStatus getStatus();
    long getCount();
}
