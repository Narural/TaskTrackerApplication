package com.example.TaskTracker.validation;

import com.example.TaskTracker.model.TaskStatus;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TaskStatusValidator implements ConstraintValidator<ValidTaskStatus, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context){
        boolean result = false;
        for (TaskStatus status: TaskStatus.values()){
            if(status.name().equals(value))
                result = true;
        }
        return result;
    }
}
