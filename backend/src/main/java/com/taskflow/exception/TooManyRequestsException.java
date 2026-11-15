package com.taskflow.exception;

import org.springframework.http.HttpStatus;

/**
 * Exceção para excesso de tentativas em endpoints protegidos por Rate Limiting (HTTP 429).
 */
public class TooManyRequestsException extends ApiException {

    public TooManyRequestsException(String detail) {
        super(HttpStatus.TOO_MANY_REQUESTS, "TOO_MANY_REQUESTS", "Too Many Requests", detail);
    }
}
