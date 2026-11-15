package com.taskflow.dto.user;

import jakarta.validation.constraints.Size;

/**
 * Payload de atualização parcial do perfil do usuário autenticado.
 */
public record UpdateProfileRequest(
        @Size(min = 1, max = 100, message = "Name must have between 1 and 100 characters.")
        String name,

        @Size(max = 64, message = "Timezone must have at most 64 characters.")
        String timezone
) {
}
