package com.taskflow.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Payload de entrada para criação de categoria.
 */
public record CreateCategoryRequest(
        @NotBlank(message = "Name is required.")
        @Size(max = 50, message = "Name must have at most 50 characters.")
        String name,

        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Color must be a valid hex code (#RRGGBB).")
        String color
) {
}
