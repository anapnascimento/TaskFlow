package com.taskflow.dto.category;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;

/**
 * Representação de categoria retornada pela API.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CategoryResponse(
        Long id,
        String name,
        String color,
        Long taskCount,
        Instant createdAt,
        Instant updatedAt
) {
}
