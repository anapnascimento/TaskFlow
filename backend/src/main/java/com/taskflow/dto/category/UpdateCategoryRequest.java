package com.taskflow.dto.category;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Payload de atualização parcial de categoria.
 */
public record UpdateCategoryRequest(
        @Size(min = 1, max = 50, message = "Name must have between 1 and 50 characters.")
        String name,

        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Color must be a valid hex code (#RRGGBB).")
        String color
) {
}
