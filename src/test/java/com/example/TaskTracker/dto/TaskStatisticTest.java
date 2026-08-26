package com.example.TaskTracker.dto;

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

    private static List<StatusCountProjection> statuses() {
        return List.of(new FakeStatusCount(TaskStatus.NEW, 3),
                       new FakeStatusCount(TaskStatus.DONE, 2));
    }

    private static List<PriorityCountProjection> priorities() {
        return List.of(new FakePriorityCount(TaskPriority.HIGH, 4),
                       new FakePriorityCount(TaskPriority.CRITICAL, 1));
    }

    @Test
    @DisplayName("Пустые проекции: все категории присутствуют с нулями, а не пустая карта")
    void taskStatistic_emptyProjections_allCategoriesAreZero() {
        TaskStatistic statistic = new TaskStatistic(List.of(), List.of());

        assertThat(statistic.getTotalTasks()).isZero();
        assertThat(statistic.getCountByStatus())
                .containsOnlyKeys(TaskStatus.values())
                .allSatisfy((status, count) -> assertThat(count).isZero());
        assertThat(statistic.getCountByPriority())
                .containsOnlyKeys(TaskPriority.values())
                .allSatisfy((priority, count) -> assertThat(count).isZero());
    }

    @Test
    @DisplayName("Пришедшие из базы категории заполняются своими значениями")
    void taskStatistic_filledProjections_fillsGivenCategories() {
        TaskStatistic statistic = new TaskStatistic(statuses(), priorities());

        assertThat(statistic.getCountByStatus())
                .containsEntry(TaskStatus.NEW, 3L)
                .containsEntry(TaskStatus.DONE, 2L);
        assertThat(statistic.getCountByPriority())
                .containsEntry(TaskPriority.HIGH, 4L)
                .containsEntry(TaskPriority.CRITICAL, 1L);
    }

    @Test
    @DisplayName("Категории, которых нет в проекциях, получают ноль")
    void taskStatistic_partialProjections_missingCategoriesAreZero() {
        TaskStatistic statistic = new TaskStatistic(statuses(), priorities());

        assertThat(statistic.getCountByStatus())
                .containsOnlyKeys(TaskStatus.values())
                .containsEntry(TaskStatus.IN_PROCESS, 0L)
                .containsEntry(TaskStatus.CANCELED, 0L);
        assertThat(statistic.getCountByPriority())
                .containsOnlyKeys(TaskPriority.values())
                .containsEntry(TaskPriority.LOW, 0L)
                .containsEntry(TaskPriority.MEDIUM, 0L);
    }

    @Test
    @DisplayName("totalTasks равен сумме значений countByStatus")
    void taskStatistic_filledProjections_totalEqualsSumOfStatusCounts() {
        TaskStatistic statistic = new TaskStatistic(statuses(), priorities());

        long sumByStatus = statistic.getCountByStatus().values().stream()
                .mapToLong(Long::longValue).sum();

        assertThat(statistic.getTotalTasks()).isEqualTo(5);
        assertThat(statistic.getTotalTasks()).isEqualTo(sumByStatus);
    }
}
