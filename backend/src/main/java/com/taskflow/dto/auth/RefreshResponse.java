package com.taskflow.dto.auth;

/**
 * Resposta de renovação de sessão com novo Access Token.
 */
public record RefreshResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {
}
