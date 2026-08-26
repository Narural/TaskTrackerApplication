package com.example.TaskTracker.controller;

import com.example.TaskTracker.exception.ChangeStatusException;
import com.example.TaskTracker.exception.ExistingTagException;
import com.example.TaskTracker.exception.FoundTaskException;
import com.example.TaskTracker.exception.NotExistingTagExceptions;
import com.example.TaskTracker.dto.ErrorResponse;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(FoundTaskException.class)
    public ResponseEntity<ErrorResponse> foundTaskHandler(FoundTaskException e){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
    }
    @ExceptionHandler(ChangeStatusException.class)
    public ResponseEntity<ErrorResponse> changeStatusHandler(ChangeStatusException e){
        return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e){
        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");
        return ResponseEntity.badRequest().body(new ErrorResponse(message));
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException e){
        return ResponseEntity.badRequest().body(new ErrorResponse("Неверный реквест"));
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException e){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("Нарушение уникальности данных"));
    }
    @ExceptionHandler(ExistingTagException.class)
    public ResponseEntity<ErrorResponse> duplicateTagHandler(ExistingTagException e){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e){
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("Некорректное значение параметра '" + e.getName() + "'"));
    }
    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ErrorResponse> handleBadSort(PropertyReferenceException e){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("Неизвестное поле сортировки: " + e.getPropertyName()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResource(NoResourceFoundException e){
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("Ресурс не найден"));
    }
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception e){
        log.error("Необработанное исключение", e);
        return ResponseEntity.internalServerError().body(new ErrorResponse("Внутренняя ошибка сервера"));
    }
    @ExceptionHandler(NotExistingTagExceptions.class)
    public ResponseEntity<ErrorResponse> notExistingTagHandler(NotExistingTagExceptions e){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
    }
}
