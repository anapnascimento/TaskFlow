package com.taskflow.dto.auth;

/**
 * Resposta de autenticação com Access Token e resumo do usuário.
 */
public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UserResponse user
) {
}
