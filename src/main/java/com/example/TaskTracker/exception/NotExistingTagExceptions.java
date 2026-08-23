package com.example.TaskTracker.exception;

public class NotExistingTagExceptions extends RuntimeException {
    public NotExistingTagExceptions() {
        super("Тэга для удаления не существует");
    }
}
