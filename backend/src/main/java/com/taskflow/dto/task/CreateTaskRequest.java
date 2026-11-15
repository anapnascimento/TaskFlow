package com.taskflow.dto.task;

import com.taskflow.enums.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

/**
 * Payload de entrada para criação de tarefa.
 */
public record CreateTaskRequest(
        @NotBlank(message = "Title is required.")
        @Size(max = 150, message = "Title must have at most 150 characters.")
        String title,

        @Size(max = 5000, message = "Description must have at most 5000 characters.")
        String description,

        Long categoryId,

        TaskPriority priority,

        Instant dueDate
) {
}
