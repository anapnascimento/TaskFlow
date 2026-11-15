package com.taskflow.service;

import com.taskflow.dto.category.CategoryListResponse;
import com.taskflow.dto.category.CategoryResponse;
import com.taskflow.dto.category.CreateCategoryRequest;
import com.taskflow.dto.category.UpdateCategoryRequest;
import com.taskflow.entity.Category;
import com.taskflow.entity.User;
import com.taskflow.enums.ActivityAction;
import com.taskflow.exception.BadRequestException;
import com.taskflow.exception.ConflictException;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.exception.UnauthorizedException;
import com.taskflow.repository.CategoryRepository;
import com.taskflow.repository.TaskRepository;
import com.taskflow.repository.UserRepository;
import com.taskflow.security.SecurityUtils;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serviço responsável pelo gerenciamento de categorias personalizadas por usuário.
 */
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ActivityLogService activityLogService;

    @Transactional
    public CategoryResponse create(CreateCategoryRequest request) {
        User user = getActiveCurrentUser();
        String normalizedName = normalizeName(request.name());

        if (categoryRepository.existsByUserIdAndNameIgnoreCaseAndDeletedAtIsNull(user.getId(), normalizedName)) {
            throw new ConflictException("CATEGORY_ALREADY_EXISTS", "A category with this name already exists.");
        }

        Category category = Category.builder()
                .user(user)
                .name(normalizedName)
                .color(request.color())
                .build();

        Category saved = categoryRepository.save(category);
        activityLogService.record(
                user,
                null,
                ActivityAction.CATEGORY_CREATED,
                "Category '" + saved.getName() + "' created",
                Map.of("categoryId", saved.getId(), "name", saved.getName())
        );
        return toResponse(saved, null);
    }

    @Transactional(readOnly = true)
    public CategoryResponse getById(Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        Category category = categoryRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found."));
        return toResponse(category, null);
    }

    @Transactional(readOnly = true)
    public CategoryListResponse list() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<Category> categories = categoryRepository.findAllByUserIdAndDeletedAtIsNullOrderByNameAsc(userId);

        Map<Long, Long> taskCounts = new HashMap<>();
        for (Object[] row : categoryRepository.countActiveTasksByCategory(userId)) {
            taskCounts.put((Long) row[0], (Long) row[1]);
        }

        List<CategoryResponse> items = categories.stream()
                .map(category -> toResponse(category, taskCounts.getOrDefault(category.getId(), 0L)))
                .toList();
        return new CategoryListResponse(items);
    }

    @Transactional
    public CategoryResponse update(Long id, UpdateCategoryRequest request) {
        User user = getActiveCurrentUser();
        Category category = categoryRepository.findByIdAndUserIdAndDeletedAtIsNull(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found."));

        if (request.name() != null) {
            String normalizedName = normalizeName(request.name());
            if (categoryRepository.existsByUserIdAndNameIgnoreCaseAndDeletedAtIsNullAndIdNot(
                    user.getId(), normalizedName, id)) {
                throw new ConflictException("CATEGORY_ALREADY_EXISTS", "A category with this name already exists.");
            }
            category.setName(normalizedName);
        }

        if (request.color() != null) {
            category.setColor(request.color());
        }

        Category saved = categoryRepository.save(category);
        activityLogService.record(
                user,
                null,
                ActivityAction.CATEGORY_UPDATED,
                "Category '" + saved.getName() + "' updated",
                Map.of("categoryId", saved.getId(), "name", saved.getName())
        );
        return toResponse(saved, null);
    }

    @Transactional
    public void delete(Long id) {
        User user = getActiveCurrentUser();
        Category category = categoryRepository.findByIdAndUserIdAndDeletedAtIsNull(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found."));

        Instant now = Instant.now();
        category.setDeletedAt(now);
        categoryRepository.save(category);
        taskRepository.clearCategoryFromUserTasks(user.getId(), category.getId(), now);

        activityLogService.record(
                user,
                null,
                ActivityAction.CATEGORY_DELETED,
                "Category '" + category.getName() + "' deleted",
                Map.of("categoryId", category.getId(), "name", category.getName())
        );
    }

    @Transactional
    public CategoryResponse restore(Long id) {
        User user = getActiveCurrentUser();
        Category category = categoryRepository.findByIdAndUserIdAndDeletedAtIsNotNull(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Deleted category not found."));

        if (categoryRepository.existsByUserIdAndNameIgnoreCaseAndDeletedAtIsNull(user.getId(), category.getName())) {
            throw new ConflictException("CATEGORY_ALREADY_EXISTS", "An active category with this name already exists.");
        }

        category.setDeletedAt(null);
        Category saved = categoryRepository.save(category);
        activityLogService.record(
                user,
                null,
                ActivityAction.CATEGORY_RESTORED,
                "Category '" + saved.getName() + "' restored",
                Map.of("categoryId", saved.getId(), "name", saved.getName())
        );
        return toResponse(saved, null);
    }

    private CategoryResponse toResponse(Category category, Long taskCount) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getColor(),
                taskCount,
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }

    private String normalizeName(String rawName) {
        String normalized = rawName.trim().replaceAll("\\s+", " ");
        if (normalized.isEmpty()) {
            throw new BadRequestException("VALIDATION_ERROR", "Category name cannot be blank.");
        }
        if (normalized.length() > 50) {
            throw new BadRequestException("VALIDATION_ERROR", "Name must have at most 50 characters.");
        }
        return normalized;
    }

    private User getActiveCurrentUser() {
        Long userId = SecurityUtils.getCurrentUserId();
        return userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new UnauthorizedException("UNAUTHORIZED", "Authenticated user not found."));
    }
}
