package com.example.TaskTracker.repository;

import com.example.TaskTracker.model.Task;
import com.example.TaskTracker.model.TaskPriority;
import com.example.TaskTracker.model.TaskStatus;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Фильтры списка задач. Незаданный фильтр не добавляет условие в SQL вообще —
 * поэтому в запрос никогда не уходит null-параметр, тип которого база не может вывести.
 */
public final class TaskSpecifications {

    private TaskSpecifications() {
    }

    /**
     * Задачи одного владельца. Владелец — обязательное условие, а не ещё один необязательный
     * фильтр: забыть его нельзя, метод без имени просто не вызовешь. Условие уходит в SQL,
     * чужие строки базу не покидают.
     */
    public static Specification<Task> ownedBy(String username, TaskStatus status, TaskPriority priority,
                                              String title, String tag) {
        Objects.requireNonNull(username, "username");
        return (root, query, builder) -> {
            List<Predicate> predicates = filters(root, builder, status, priority, title, tag);
            predicates.add(builder.equal(root.get("owner").get("username"), username));
            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /** Задачи всех пользователей — только для административного списка. */
    public static Specification<Task> filter(TaskStatus status, TaskPriority priority,
                                             String title, String tag) {
        return (root, query, builder) ->
                builder.and(filters(root, builder, status, priority, title, tag).toArray(new Predicate[0]));
    }

    private static List<Predicate> filters(Root<Task> root, CriteriaBuilder builder, TaskStatus status,
                                           TaskPriority priority, String title, String tag) {
        List<Predicate> predicates = new ArrayList<>();

        if (status != null) {
            predicates.add(builder.equal(root.get("status"), status));
        }
        if (priority != null) {
            predicates.add(builder.equal(root.get("priority"), priority));
        }
        if (title != null && !title.isBlank()) {
            predicates.add(builder.like(
                    builder.lower(root.get("title")),
                    "%" + title.toLowerCase() + "%"));
        }
        if (tag != null && !tag.isBlank()) {
            // пара (task_id, tag) уникальна, поэтому соединение не размножает задачи
            predicates.add(builder.equal(root.join("tags"), tag));
        }
        return predicates;
    }
}
