package com.example.TaskTracker.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MaxUtf8BytesValidatorTest {

    private final MaxUtf8BytesValidator validator = new MaxUtf8BytesValidator();

    @BeforeEach
    void limitIs72Bytes() {
        MaxUtf8Bytes annotation = mock(MaxUtf8Bytes.class);
        when(annotation.value()).thenReturn(72);
        validator.initialize(annotation);
    }

    @Test
    void isValid_latin72Chars_isValid() {
        assertThat(validator.isValid("a".repeat(72), null)).isTrue();
    }

    @Test
    void isValid_cyrillic36CharsIs72Bytes_isValid() {
        assertThat(validator.isValid("ж".repeat(36), null)).isTrue();
    }

    @Test
    void isValid_cyrillic37CharsIs74Bytes_isInvalid() {
        assertThat(validator.isValid("ж".repeat(37), null)).isFalse();
    }

    @Test
    void isValid_null_leftToNotBlank() {
        assertThat(validator.isValid(null, null)).isTrue();
    }
}
