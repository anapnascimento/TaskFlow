package com.taskflow.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Payload para solicitação de recuperação de senha.
 */
public record ForgotPasswordRequest(
        @NotBlank(message = "Email is required.")
        @Email(message = "Email must be valid.")
        String email
) {
}
