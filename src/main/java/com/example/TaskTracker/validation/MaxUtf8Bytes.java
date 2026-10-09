package com.example.TaskTracker.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Ограничение длины строки в байтах UTF-8, а не в символах.
 * Нужно для пароля: BCrypt принимает не больше 72 байт, а русская буква занимает 2 байта,
 * поэтому @Size(max = 70) пропускал 40 русских букв (80 байт) и регистрация падала с 500.
 */
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
