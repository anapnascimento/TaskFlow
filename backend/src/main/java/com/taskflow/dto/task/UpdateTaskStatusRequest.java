package com.taskflow.dto.task;

import com.taskflow.enums.TaskStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Payload de alteração de status de uma tarefa.
 */
public record UpdateTaskStatusRequest(
        @NotNull(message = "Status is required.")
        TaskStatus status
) {
}
