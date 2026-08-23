package com.example.TaskTracker.exception;

public class ExistingTagException extends RuntimeException {
    public ExistingTagException() {
        super("Данный тэг был уже добавлен");
    }
}
