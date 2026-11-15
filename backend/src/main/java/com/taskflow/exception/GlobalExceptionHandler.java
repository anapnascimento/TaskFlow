package com.taskflow.exception;

import com.taskflow.exception.ProblemDetailResponse.FieldErrorDetail;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Tratador global de exceções que converte erros em respostas Problem Details (RFC 9457).
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ProblemDetailResponse> handleApiException(
            ApiException ex, HttpServletRequest request) {
        ProblemDetailResponse body = new ProblemDetailResponse(
                "about:blank",
                ex.getTitle(),
                ex.getStatus().value(),
                ex.getMessage(),
                request.getRequestURI(),
                ex.getCode(),
                Instant.now(),
                null
        );
        return ResponseEntity.status(ex.getStatus())
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetailResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<FieldErrorDetail> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldErrorDetail(error.getField(), error.getDefaultMessage()))
                .toList();

        ProblemDetailResponse body = new ProblemDetailResponse(
                "about:blank",
                "Validation Error",
                HttpStatus.BAD_REQUEST.value(),
                "One or more fields are invalid.",
                request.getRequestURI(),
                "VALIDATION_ERROR",
                Instant.now(),
                fieldErrors
        );
        return ResponseEntity.badRequest()
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetailResponse> handleUnreadableBody(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        ProblemDetailResponse body = new ProblemDetailResponse(
                "about:blank",
                "Malformed Request",
                HttpStatus.BAD_REQUEST.value(),
                "Request body is missing or malformed.",
                request.getRequestURI(),
                "MALFORMED_REQUEST",
                Instant.now(),
                null
        );
        return ResponseEntity.badRequest()
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetailResponse> handleUnexpected(
            Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        ProblemDetailResponse body = new ProblemDetailResponse(
                "about:blank",
                "Internal Server Error",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred.",
                request.getRequestURI(),
                "INTERNAL_ERROR",
                Instant.now(),
                null
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(body);
    }
}
