package com.taskflow.dto.task;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.taskflow.enums.TaskPriority;
import com.taskflow.enums.TaskStatus;
import java.time.Instant;

/**
 * Representação completa de tarefa retornada pela API.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TaskResponse(
        Long id,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        TaskCategorySummary category,
        Instant dueDate,
        Instant completedAt,
        boolean overdue,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt
) {
}
