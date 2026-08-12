package com.example.TaskTracker;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TaskStatusValidator.class)
@Documented
public @interface ValidTaskStatus {
    String message() default "Неизвестный статус задачи";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
