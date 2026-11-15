package com.taskflow.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Payload para redefinição de senha usando token de uso único.
 */
public record ResetPasswordRequest(
        @NotBlank(message = "Token is required.")
        String token,

        @NotBlank(message = "New password is required.")
        @Size(min = 8, max = 128, message = "New password must have between 8 and 128 characters.")
        String newPassword
) {
}
