package com.example.TaskTracker.repository;

import com.example.TaskTracker.model.Task;
import com.example.TaskTracker.model.TaskPriority;
import com.example.TaskTracker.model.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    /**
     * Один вход вместо трёх производных методов: любой из фильтров может быть null,
     * тогда соответствующее условие в запрос не попадает.
     */
    default Page<Task> search(TaskStatus status, TaskPriority priority,
                              String title, Pageable pageable) {
        return findAll(TaskSpecifications.filter(status, priority, title), pageable);
    }

    @Query("select t.status as status, count(t) as count from Task t group by t.status")
    List<StatusCountProjection> countGroupedByStatus();

    @Query("select t.priority as priority, count(t) as count from Task t group by t.priority")
    List<PriorityCountProjection> countGroupedByPriority();
}
