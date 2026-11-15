package com.taskflow.dto.category;

import java.util.List;

/**
 * Resposta de listagem de categorias do usuário.
 */
public record CategoryListResponse(List<CategoryResponse> content) {
}
