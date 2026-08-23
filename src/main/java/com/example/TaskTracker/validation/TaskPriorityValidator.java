package com.example.TaskTracker.validation;

import com.example.TaskTracker.model.TaskPriority;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TaskPriorityValidator implements ConstraintValidator<ValidTaskPriority, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context){
        boolean result = false;
        for (TaskPriority priority: TaskPriority.values()){
            if(priority.name().equals(value))
                result = true;
        }
        return result;
    }
}
