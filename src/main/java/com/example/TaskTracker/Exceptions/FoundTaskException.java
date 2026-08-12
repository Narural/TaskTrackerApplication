package com.example.TaskTracker.Exceptions;

public class FoundTaskException extends RuntimeException {
    public FoundTaskException() {
        super("No tasks found");
    }
}
