package com.taskflow.exception;

import org.springframework.http.HttpStatus;

/**
 * Exceção para requisições inválidas (HTTP 400).
 */
public class BadRequestException extends ApiException {

    public BadRequestException(String code, String detail) {
        super(HttpStatus.BAD_REQUEST, code, "Bad Request", detail);
    }
}
