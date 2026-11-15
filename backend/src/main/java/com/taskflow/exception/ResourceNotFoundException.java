package com.taskflow.exception;

import org.springframework.http.HttpStatus;

/**
 * Exceção para recursos inexistentes ou inacessíveis ao usuário (HTTP 404).
 */
public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException(String detail) {
        super(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", "Not Found", detail);
    }
}
