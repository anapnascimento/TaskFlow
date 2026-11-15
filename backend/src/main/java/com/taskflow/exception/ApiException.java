package com.taskflow.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Exceção base para erros de negócio e de API padronizados via Problem Details.
 */
@Getter
public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String code;
    private final String title;

    public ApiException(HttpStatus status, String code, String title, String detail) {
        super(detail);
        this.status = status;
        this.code = code;
        this.title = title;
    }
}
