package com.taskflow.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Payload de entrada para cadastro de usuário.
 */
public record RegisterRequest(
        @NotBlank(message = "Name is required.")
        @Size(max = 100, message = "Name must have at most 100 characters.")
        String name,

        @NotBlank(message = "Email is required.")
        @Email(message = "Email must be valid.")
        @Size(max = 255, message = "Email must have at most 255 characters.")
        String email,

        @NotBlank(message = "Password is required.")
        @Size(min = 8, max = 128, message = "Password must have between 8 and 128 characters.")
        String password,

        @Size(max = 64, message = "Timezone must have at most 64 characters.")
        String timezone
) {
}
