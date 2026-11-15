package com.taskflow.dto.task;

/**
 * Resumo da categoria associada a uma tarefa.
 */
public record TaskCategorySummary(
        Long id,
        String name,
        String color
) {
}
