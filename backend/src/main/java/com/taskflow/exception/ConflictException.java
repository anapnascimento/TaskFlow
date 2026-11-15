package com.taskflow.exception;

import org.springframework.http.HttpStatus;

/**
 * Exceção para conflitos de unicidade ou estado (HTTP 409).
 */
public class ConflictException extends ApiException {

    public ConflictException(String code, String detail) {
        super(HttpStatus.CONFLICT, code, "Conflict", detail);
    }
}
