package com.example.TaskTracker.model;

public enum TaskStatus {
    NEW,
    IN_PROCESS,
    DONE,
    CANCELED;

    public boolean canMoveTo(TaskStatus target){
        return switch (this){
            case NEW -> target == TaskStatus.IN_PROCESS ||
                    target == TaskStatus.CANCELED;
            case IN_PROCESS -> target == TaskStatus.DONE
                    || target == TaskStatus.CANCELED;
            case DONE -> target == TaskStatus.IN_PROCESS || target == TaskStatus.CANCELED;
            default -> false;
        };
    }
}
