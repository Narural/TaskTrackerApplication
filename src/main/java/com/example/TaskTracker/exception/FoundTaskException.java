package com.example.TaskTracker.exception;

public class FoundTaskException extends RuntimeException {
    public FoundTaskException() {
        super("No tasks found");
    }
}
