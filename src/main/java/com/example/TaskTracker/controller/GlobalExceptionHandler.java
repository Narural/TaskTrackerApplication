package com.example.TaskTracker.controller;

import com.example.TaskTracker.dto.ErrorResponse;
import com.example.TaskTracker.exception.ChangeStatusException;
import com.example.TaskTracker.exception.ExistingTagException;
import com.example.TaskTracker.exception.FoundTaskException;
import com.example.TaskTracker.exception.NotExistingTagExceptions;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ResponseEntity<ErrorResponse> error(HttpStatus status, String code,
                                                String message, HttpServletRequest request) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.value(), code, message, request.getRequestURI()));
    }

    @ExceptionHandler(FoundTaskException.class)
    public ResponseEntity<ErrorResponse> foundTaskHandler(FoundTaskException e,
                                                          HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, "TASK_NOT_FOUND", e.getMessage(), request);
    }

    @ExceptionHandler(ChangeStatusException.class)
    public ResponseEntity<ErrorResponse> changeStatusHandler(ChangeStatusException e,
                                                             HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "STATUS_TRANSITION_NOT_ALLOWED", e.getMessage(), request);
    }

    @ExceptionHandler(ExistingTagException.class)
    public ResponseEntity<ErrorResponse> duplicateTagHandler(ExistingTagException e,
                                                             HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, "TAG_ALREADY_EXISTS", e.getMessage(), request);
    }

    @ExceptionHandler(NotExistingTagExceptions.class)
    public ResponseEntity<ErrorResponse> notExistingTagHandler(NotExistingTagExceptions e,
                                                               HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, "TAG_NOT_FOUND", e.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e,
                                                          HttpServletRequest request) {
        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", message, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException e,
                                                           HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST", "Неверный реквест", request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException e,
                                                                      HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, "DATA_INTEGRITY_VIOLATION",
                "Нарушение уникальности данных", request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e,
                                                            HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_PARAMETER",
                "Некорректное значение параметра '" + e.getName() + "'", request);
    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ErrorResponse> handleBadSort(PropertyReferenceException e,
                                                       HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "UNKNOWN_SORT_PROPERTY",
                "Неизвестное поле сортировки: " + e.getPropertyName(), request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResource(NoResourceFoundException e,
                                                          HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", "Ресурс не найден", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception e,
                                                          HttpServletRequest request) {
        log.error("Необработанное исключение", e);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "Внутренняя ошибка сервера", request);
    }
}
