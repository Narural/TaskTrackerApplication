package com.example.TaskTracker.repository;

import com.example.TaskTracker.model.Task;
import com.example.TaskTracker.model.TaskPriority;
import com.example.TaskTracker.model.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

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
