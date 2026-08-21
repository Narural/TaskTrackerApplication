package com.example.TaskTracker.Exceptions;

public class NotExistingTagExceptions extends RuntimeException {
    public NotExistingTagExceptions() {
        super("Тэга для удаления не существует");
    }
}
