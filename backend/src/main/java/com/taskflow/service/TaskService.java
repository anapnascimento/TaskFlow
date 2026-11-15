package com.taskflow.service;

import com.taskflow.dto.common.PageResponse;
import com.taskflow.dto.task.CreateTaskRequest;
import com.taskflow.dto.task.TaskCategorySummary;
import com.taskflow.dto.task.TaskResponse;
import com.taskflow.dto.task.UpdateTaskRequest;
import com.taskflow.dto.task.UpdateTaskStatusRequest;
import com.taskflow.entity.Category;
import com.taskflow.entity.Task;
import com.taskflow.entity.User;
import com.taskflow.enums.ActivityAction;
import com.taskflow.enums.TaskPriority;
import com.taskflow.enums.TaskStatus;
import com.taskflow.exception.BadRequestException;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.exception.UnauthorizedException;
import com.taskflow.repository.CategoryRepository;
import com.taskflow.repository.TaskRepository;
import com.taskflow.repository.TaskSpecification;
import com.taskflow.repository.UserRepository;
import com.taskflow.security.SecurityUtils;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serviço responsável pelas regras de negócio do gerenciamento de tarefas.
 */
@Service
@RequiredArgsConstructor
public class TaskService {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 100;
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "createdAt", "updatedAt", "dueDate", "priority", "status", "title"
    );

    private static final Map<TaskStatus, Set<TaskStatus>> ALLOWED_TRANSITIONS = Map.of(
            TaskStatus.PENDING, Set.of(TaskStatus.IN_PROGRESS, TaskStatus.COMPLETED, TaskStatus.CANCELLED),
            TaskStatus.IN_PROGRESS, Set.of(TaskStatus.PENDING, TaskStatus.COMPLETED, TaskStatus.CANCELLED),
            TaskStatus.COMPLETED, Set.of(TaskStatus.PENDING, TaskStatus.IN_PROGRESS),
            TaskStatus.CANCELLED, Set.of(TaskStatus.PENDING, TaskStatus.IN_PROGRESS)
    );

    private final TaskRepository taskRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ActivityLogService activityLogService;

    @Transactional
    public TaskResponse create(CreateTaskRequest request) {
        User user = getActiveCurrentUser();
        String title = normalizeTitle(request.title());
        Category category = resolveUserCategory(request.categoryId(), user.getId());

        Task task = Task.builder()
                .user(user)
                .category(category)
                .title(title)
                .description(request.description() != null ? request.description().trim() : null)
                .status(TaskStatus.PENDING)
                .priority(request.priority() != null ? request.priority() : TaskPriority.MEDIUM)
                .dueDate(request.dueDate())
                .build();

        Task saved = taskRepository.save(task);
        activityLogService.record(
                user,
                saved,
                ActivityAction.TASK_CREATED,
                "Task '" + saved.getTitle() + "' created",
                Map.of("title", saved.getTitle(), "priority", saved.getPriority().name())
        );
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public TaskResponse getById(Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        Task task = taskRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found."));
        return toResponse(task);
    }

    @Transactional(readOnly = true)
    public PageResponse<TaskResponse> list(
            int page,
            int size,
            String sortParam,
            boolean deleted,
            TaskStatus status,
            TaskPriority priority,
            Long categoryId,
            Instant startDate,
            Instant endDate,
            String search) {
        Long userId = SecurityUtils.getCurrentUserId();
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? DEFAULT_PAGE_SIZE : Math.min(size, MAX_PAGE_SIZE);
        Sort sort = parseSort(sortParam);
        Pageable pageable = PageRequest.of(safePage, safeSize, sort);

        Page<TaskResponse> result = taskRepository.findAll(
                TaskSpecification.withFilters(userId, deleted, status, priority, categoryId, startDate, endDate, search),
                pageable
        ).map(this::toResponse);

        return PageResponse.from(result);
    }

    @Transactional
    public TaskResponse update(Long id, UpdateTaskRequest request) {
        User user = getActiveCurrentUser();
        Task task = taskRepository.findByIdAndUserIdAndDeletedAtIsNull(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found."));

        if (request.getTitle() != null) {
            task.setTitle(normalizeTitle(request.getTitle()));
        }
        if (request.isDescriptionProvided()) {
            task.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        }
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        if (request.isCategoryIdProvided()) {
            task.setCategory(resolveUserCategory(request.getCategoryId(), user.getId()));
        }
        if (request.isDueDateProvided()) {
            task.setDueDate(request.getDueDate());
        }

        Task saved = taskRepository.save(task);
        activityLogService.record(
                user,
                saved,
                ActivityAction.TASK_UPDATED,
                "Task '" + saved.getTitle() + "' updated",
                Map.of("title", saved.getTitle(), "priority", saved.getPriority().name())
        );
        return toResponse(saved);
    }

    @Transactional
    public TaskResponse changeStatus(Long id, UpdateTaskStatusRequest request) {
        User user = getActiveCurrentUser();
        Task task = taskRepository.findByIdAndUserIdAndDeletedAtIsNull(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found."));

        TaskStatus previousStatus = task.getStatus();
        TaskStatus nextStatus = request.status();

        if (previousStatus == nextStatus || !ALLOWED_TRANSITIONS.getOrDefault(previousStatus, Set.of()).contains(nextStatus)) {
            throw new BadRequestException(
                    "INVALID_STATUS_TRANSITION",
                    "Transition from " + previousStatus + " to " + nextStatus + " is not allowed."
            );
        }

        task.setStatus(nextStatus);
        if (nextStatus == TaskStatus.COMPLETED) {
            task.setCompletedAt(Instant.now());
        } else if (previousStatus == TaskStatus.COMPLETED) {
            task.setCompletedAt(null);
        }

        Task saved = taskRepository.save(task);

        ActivityAction action = switch (nextStatus) {
            case COMPLETED -> ActivityAction.TASK_COMPLETED;
            case CANCELLED -> ActivityAction.TASK_CANCELLED;
            default -> ActivityAction.TASK_STATUS_CHANGED;
        };

        activityLogService.record(
                user,
                saved,
                action,
                "Task '" + saved.getTitle() + "' status changed from " + previousStatus + " to " + nextStatus,
                Map.of("previousStatus", previousStatus.name(), "newStatus", nextStatus.name())
        );
        return toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        User user = getActiveCurrentUser();
        Task task = taskRepository.findByIdAndUserIdAndDeletedAtIsNull(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found."));

        task.setDeletedAt(Instant.now());
        Task saved = taskRepository.save(task);
        activityLogService.record(
                user,
                saved,
                ActivityAction.TASK_DELETED,
                "Task '" + saved.getTitle() + "' moved to trash",
                Map.of("title", saved.getTitle())
        );
    }

    @Transactional
    public TaskResponse restore(Long id) {
        User user = getActiveCurrentUser();
        Task task = taskRepository.findByIdAndUserIdAndDeletedAtIsNotNull(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Deleted task not found."));

        task.setDeletedAt(null);
        Task saved = taskRepository.save(task);
        activityLogService.record(
                user,
                saved,
                ActivityAction.TASK_RESTORED,
                "Task '" + saved.getTitle() + "' restored from trash",
                Map.of("title", saved.getTitle())
        );
        return toResponse(saved);
    }

    private Category resolveUserCategory(Long categoryId, Long userId) {
        if (categoryId == null) {
            return null;
        }
        return categoryRepository.findByIdAndUserIdAndDeletedAtIsNull(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found."));
    }

    private String normalizeTitle(String rawTitle) {
        String normalized = rawTitle.trim().replaceAll("\\s+", " ");
        if (normalized.isEmpty()) {
            throw new BadRequestException("VALIDATION_ERROR", "Title cannot be blank.");
        }
        if (normalized.length() > 150) {
            throw new BadRequestException("VALIDATION_ERROR", "Title must have at most 150 characters.");
        }
        return normalized;
    }

    private Sort parseSort(String sortParam) {
        if (sortParam == null || sortParam.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        String[] parts = sortParam.split(",");
        String field = parts[0].trim();
        if (!ALLOWED_SORT_FIELDS.contains(field)) {
            throw new BadRequestException("INVALID_SORT_FIELD", "Sorting by '" + field + "' is not allowed.");
        }
        Sort.Direction direction = (parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim()))
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        return Sort.by(direction, field);
    }

    private TaskResponse toResponse(Task task) {
        TaskCategorySummary categorySummary = null;
        if (task.getCategory() != null && task.getCategory().getDeletedAt() == null) {
            categorySummary = new TaskCategorySummary(
                    task.getCategory().getId(),
                    task.getCategory().getName(),
                    task.getCategory().getColor()
            );
        }

        boolean overdue = task.getDueDate() != null
                && task.getDueDate().isBefore(Instant.now())
                && task.getStatus() != TaskStatus.COMPLETED
                && task.getStatus() != TaskStatus.CANCELLED;

        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                categorySummary,
                task.getDueDate(),
                task.getCompletedAt(),
                overdue,
                task.getCreatedAt(),
                task.getUpdatedAt(),
                task.getDeletedAt()
        );
    }

    private User getActiveCurrentUser() {
        Long userId = SecurityUtils.getCurrentUserId();
        return userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new UnauthorizedException("UNAUTHORIZED", "Authenticated user not found."));
    }
}
