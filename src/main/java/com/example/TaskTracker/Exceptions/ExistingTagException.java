package com.example.TaskTracker.Exceptions;

public class ExistingTagException extends RuntimeException {
    public ExistingTagException() {
        super("Данный тэг был уже добавлен");
    }
}
