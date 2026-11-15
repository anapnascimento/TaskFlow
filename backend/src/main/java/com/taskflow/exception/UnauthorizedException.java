package com.taskflow.exception;

import org.springframework.http.HttpStatus;

/**
 * Exceção para falhas de autenticação ou sessão inválida (HTTP 401).
 */
public class UnauthorizedException extends ApiException {

    public UnauthorizedException(String code, String detail) {
        super(HttpStatus.UNAUTHORIZED, code, "Unauthorized", detail);
    }
}
