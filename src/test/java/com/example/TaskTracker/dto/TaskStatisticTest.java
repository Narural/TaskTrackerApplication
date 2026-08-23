package com.example.TaskTracker.dto;

import com.example.TaskTracker.model.Task;
import com.example.TaskTracker.model.TaskPriority;
import com.example.TaskTracker.model.TaskStatus;
import com.example.TaskTracker.repository.PriorityCountProjection;
import com.example.TaskTracker.repository.StatusCountProjection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TaskStatisticTest {
    private record FakeStatusCount(TaskStatus getStatus, long getCount) implements StatusCountProjection {}
    private record FakePriorityCount(TaskPriority getPriority, long getCount) implements PriorityCountProjection {}

    @Test
    @DisplayName("Пустые мапы правильно обрабатываются,"+
            "тоталтаск принимает передаваемое значение")
    void taskStatisticConstructor_emptyMapsAndTotalTask_returnsEmptyMaps(){
        TaskStatistic taskStatistic = new TaskStatistic(0, List.of(), List.of());
        assertThat(taskStatistic.getTotalTasks()).isEqualTo(0);
        assertThat(taskStatistic.getCountByStatus()).isEmpty();
        assertThat(taskStatistic.getCountByPriority()).isEmpty();
    }

    @Test
    @DisplayName("Маппы корректно заполняются")
    void taskStatisticConstructor_filledProjections_returnsCorrectFilledMaps(){
        List<StatusCountProjection> byStatus =
                List.of(new FakeStatusCount(TaskStatus.NEW, 3),
                        new FakeStatusCount(TaskStatus.DONE, 2));
        List<PriorityCountProjection> byPriority =
                List.of(new FakePriorityCount(TaskPriority.HIGH, 4),
                        new FakePriorityCount(TaskPriority.CRITICAL, 1));
        TaskStatistic taskStatistic = new TaskStatistic(5, byStatus, byPriority);
        assertThat(taskStatistic.getTotalTasks()).isEqualTo(5);
        assertThat(taskStatistic.getCountByStatus()).
                containsEntry(TaskStatus.NEW, 3L).containsEntry(TaskStatus.DONE, 2L);
        assertThat(taskStatistic.getCountByPriority())
                .containsEntry(TaskPriority.HIGH, 4L).containsEntry(TaskPriority.CRITICAL, 1L);
    }


}
