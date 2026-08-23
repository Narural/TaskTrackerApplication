package com.example.TaskTracker.model;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class TaskStatusTest {
    @ParameterizedTest(name = "{0} -> {1} разрешён")
    @CsvSource({"NEW, IN_PROCESS", "NEW, CANCELED", "IN_PROCESS, DONE",
            "IN_PROCESS, CANCELED", "DONE, CANCELED", "DONE, IN_PROCESS"})
    void canMoveTo_allowedTransition_returnTrue(TaskStatus from, TaskStatus to){
        assertThat(from.canMoveTo(to)).isTrue();
    }

    @ParameterizedTest(name = "{0} -> {1} запрещён")
    @CsvSource({"NEW, DONE", "IN_PROCESS, NEW", "DONE, NEW", "CANCELED, NEW",
            "CANCELED, IN_PROCESS", "CANCELED, DONE"})
    void canMoveTo_forbiddenTransition_returnFalse(TaskStatus from, TaskStatus to){
        assertThat(from.canMoveTo(to)).isFalse();
    }

    @Test
    @DisplayName("Из Canceled нет ниодного разрешённого перехода")
    void canMoveTo_fromCanceled_alwaysFalse(){
        for(TaskStatus target: TaskStatus.values())
            assertThat(TaskStatus.CANCELED.canMoveTo(target)).isFalse();
    }
}
