package com.example.TaskTracker.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER,
        ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TaskStatusValidator.class)
@Documented
public @interface ValidTaskStatus {
    String message() default "Неизвестный статус задачи";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
