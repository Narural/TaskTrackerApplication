package com.example.TaskTracker.repository;

import com.example.TaskTracker.model.Task;
import com.example.TaskTracker.model.TaskPriority;
import com.example.TaskTracker.model.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    /**
     * Задачи одного владельца с необязательными фильтрами.
     * Любой фильтр может быть null — тогда его условие в запрос не попадает.
     */
    default Page<Task> searchOwned(String username, TaskStatus status, TaskPriority priority,
                                   String title, String tag, Pageable pageable) {
        return findAll(TaskSpecifications.ownedBy(username, status, priority, title, tag), pageable);
    }

    /** Задачи всех пользователей — для администратора. */
    default Page<Task> searchAll(TaskStatus status, TaskPriority priority,
                                 String title, String tag, Pageable pageable) {
        return findAll(TaskSpecifications.filter(status, priority, title, tag), pageable);
    }

    /** Чужая задача и несуществующая неотличимы: обе дают пустой Optional. */
    Optional<Task> findByIdAndOwnerUsername(Long id, String username);

    @Query("select t.status as status, count(t) as count from Task t "
            + "where t.owner.username = :username group by t.status")
    List<StatusCountProjection> countGroupedByStatus(@Param("username") String username);

    @Query("select t.priority as priority, count(t) as count from Task t "
            + "where t.owner.username = :username group by t.priority")
    List<PriorityCountProjection> countGroupedByPriority(@Param("username") String username);
}
