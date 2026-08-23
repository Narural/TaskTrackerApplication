package com.example.TaskTracker.validation;

import com.example.TaskTracker.model.TaskPriority;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
public class TaskPriorityValidatorTest {
    private final TaskPriorityValidator validator = new TaskPriorityValidator();

    @ParameterizedTest(name = "{0} - проходит валидацию")
    @EnumSource(TaskPriority.class)
    void isValid_validDataInput_isTrue(TaskPriority priority){
        boolean result = validator.isValid(priority.name(), null);
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Неверный параметр валидацию не проходит")
    void isValid_invalidDataInput_isFalse(){
        boolean result = validator.isValid("a", null);
        assertThat(result).isFalse();
    }
    @Test
    @DisplayName("Пустой параметр валидацию не проходит")
    void isValid_EmptyDataInput_isFalse(){
        boolean result = validator.isValid("", null);
        assertThat(result).isFalse();
    }
    @Test
    @DisplayName("null параметр валидацию не проходит")
    void isValid_nullDataInput_isFalse(){
        boolean result = validator.isValid(null, null);
        assertThat(result).isFalse();
    }
    @ParameterizedTest(name = "{0} - в нижнем регистре не проходит валидацию")
    @EnumSource(TaskPriority.class)
    void isValid_wrongRegisterDataInput_isFalse(TaskPriority priority){
        boolean result = validator.isValid(priority.name().toLowerCase(), null);
        assertThat(result).isFalse();
    }
}