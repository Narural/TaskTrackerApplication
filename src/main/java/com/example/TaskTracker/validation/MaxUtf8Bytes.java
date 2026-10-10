package com.example.TaskTracker.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER,
        ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MaxUtf8BytesValidator.class)
@Documented
public @interface MaxUtf8Bytes {
    int value();
    String message() default "Слишком длинное значение";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
