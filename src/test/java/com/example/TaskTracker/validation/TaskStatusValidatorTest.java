package com.example.TaskTracker.validation;

import com.example.TaskTracker.model.TaskStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
public class TaskStatusValidatorTest {
    private final TaskStatusValidator validator = new TaskStatusValidator();

    @ParameterizedTest(name = "{0} - проходит валидацию")
    @EnumSource(TaskStatus.class)
    void isValid_validDataInput_isTrue(TaskStatus status){
        boolean result = validator.isValid(status.name(), null);
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
    @EnumSource(TaskStatus.class)
    void isValid_wrongRegisterDataInput_isFalse(TaskStatus status){
        boolean result = validator.isValid(status.name().toLowerCase(), null);
        assertThat(result).isFalse();
    }
}
