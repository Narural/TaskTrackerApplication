package com.example.TaskTracker;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TaskPriorityValidator.class)
@Documented
public @interface ValidTaskPriority {
    String message() default "Неизвестный статус задачи";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
