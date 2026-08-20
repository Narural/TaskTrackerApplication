package com.example.TaskTracker;

import com.example.TaskTracker.Enums.TaskPriority;
import com.example.TaskTracker.Enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public interface TaskRepository extends JpaRepository<Task, Long> {

    Page<Task> findByStatus(TaskStatus status, Pageable pageable);

    Page<Task> findByStatusAndPriority(
            TaskStatus status,
            TaskPriority priority,
            Pageable pageable
    );
    @Query("select t.status as status, count(t) as count from Task t group by t.status")
    List<StatusCountProjection> countGroupedByStatus();

    @Query("select t.priority as priority, count(t) as count from Task t group by t.priority")
    List<PriorityCountProjection> countGroupedByPriority();
    Page<Task> findByTitleContainingIgnoreCase(String title, Pageable pageable);
}
