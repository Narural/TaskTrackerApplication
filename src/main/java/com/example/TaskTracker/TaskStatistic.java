package com.example.TaskTracker;

import com.example.TaskTracker.Enums.TaskPriority;
import com.example.TaskTracker.Enums.TaskStatus;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class TaskStatistic {
    private final long totalTasks;
    private final Map<TaskPriority, Long> countByPriority;
    private final Map<TaskStatus, Long> countByStatus;

    public TaskStatistic(long totalTasks, List<StatusCountProjection> byStatus,
                         List<PriorityCountProjection> byPriority){
        this.totalTasks =  totalTasks;
        this.countByPriority = new EnumMap<>(TaskPriority.class);
        for(PriorityCountProjection p : byPriority)
            countByPriority.put(p.getPriority() ,p.getCount());

        this.countByStatus = new EnumMap<>(TaskStatus.class);
        for (StatusCountProjection s: byStatus)
            countByStatus.put(s.getStatus(), s.getCount());
    }

    public long getTotalTasks() {
        return totalTasks;
    }

    public Map<TaskPriority, Long> getCountByPriority() {
        return countByPriority;
    }

    public Map<TaskStatus, Long> getCountByStatus() {
        return countByStatus;
    }
}
