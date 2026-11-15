package com.taskflow.dto.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;

/**
 * Representação pública de dados do usuário autenticado.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserResponse(
        Long id,
        String name,
        String email,
        String timezone,
        Instant createdAt
) {
}
