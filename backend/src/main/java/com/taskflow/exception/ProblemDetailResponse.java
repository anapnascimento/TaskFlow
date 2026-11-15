package com.taskflow.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;

/**
 * Estrutura padronizada de erros baseada em RFC 9457 (Problem Details).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProblemDetailResponse(
        String type,
        String title,
        int status,
        String detail,
        String instance,
        String code,
        Instant timestamp,
        List<FieldErrorDetail> errors
) {
    public record FieldErrorDetail(String field, String message) {
    }
}
