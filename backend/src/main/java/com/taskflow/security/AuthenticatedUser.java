package com.taskflow.security;

/**
 * Principal autenticado armazenado no SecurityContext após validação do Access Token.
 */
public record AuthenticatedUser(
        Long id,
        String email,
        String name,
        String timezone
) {
}
