package com.example.TaskTracker.exception;

public class ChangeStatusException extends RuntimeException {
    public ChangeStatusException() {
        super("Ошибка изменения статуса, статус можно менять только на 1 шаг");
    }
}
