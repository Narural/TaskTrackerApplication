package com.example.TaskTracker.repository;

import com.example.TaskTracker.model.Task;
import com.example.TaskTracker.model.TaskPriority;
import com.example.TaskTracker.model.TaskStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Фильтры списка задач. Незаданный фильтр не добавляет условие в SQL вообще —
 * поэтому в запрос никогда не уходит null-параметр, тип которого база не может вывести.
 */
public final class TaskSpecifications {

    private TaskSpecifications() {
    }

    public static Specification<Task> filter(TaskStatus status, TaskPriority priority, String title) {
        return (root, query, builder) -> {
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
            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
