package com.taskflow.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Payload para alteração de senha do usuário autenticado.
 */
public record ChangePasswordRequest(
        @NotBlank(message = "Current password is required.")
        String currentPassword,

        @NotBlank(message = "New password is required.")
        @Size(min = 8, max = 128, message = "New password must have between 8 and 128 characters.")
        String newPassword
) {
}
