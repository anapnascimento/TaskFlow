package com.taskflow.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.taskflow.entity.ActivityLog;
import com.taskflow.entity.Category;
import com.taskflow.entity.PasswordResetToken;
import com.taskflow.entity.RefreshToken;
import com.taskflow.entity.Task;
import com.taskflow.entity.User;
import com.taskflow.enums.ActivityAction;
import com.taskflow.enums.TaskPriority;
import com.taskflow.enums.TaskStatus;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;

@SpringBootTest
@Testcontainers
@Transactional
class PersistenceIntegrationTest {

    @Container
    @ServiceConnection
    static MySQLContainer mysql = new MySQLContainer("mysql:8.4");

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ActivityLogRepository activityLogRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Test
    void shouldPersistEntitiesWithRelationshipsAndUserIsolation() {
        User user = userRepository.save(User.builder()
                .name("Maria Silva")
                .email("maria@example.com")
                .passwordHash("$argon2id$hash")
                .build());

        assertThat(user.getId()).isNotNull();
        assertThat(user.getTimezone()).isEqualTo("America/Sao_Paulo");
        assertThat(user.getCreatedAt()).isNotNull();

        Category category = categoryRepository.save(Category.builder()
                .user(user)
                .name("Estudos")
                .color("#6366F1")
                .build());

        Task task = taskRepository.save(Task.builder()
                .user(user)
                .category(category)
                .title("Estudar Spring Data JPA")
                .description("Validar migrations e queries")
                .priority(TaskPriority.HIGH)
                .dueDate(Instant.now().plus(2, ChronoUnit.DAYS))
                .build());

        assertThat(task.getStatus()).isEqualTo(TaskStatus.PENDING);

        ActivityLog log = activityLogRepository.save(ActivityLog.builder()
                .user(user)
                .task(task)
                .action(ActivityAction.TASK_CREATED)
                .description("Task created")
                .metadata(Map.of("title", task.getTitle()))
                .build());

        assertThat(log.getId()).isNotNull();
        assertThat(activityLogRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId(), PageRequest.of(0, 10))
                .getTotalElements()).isEqualTo(1);

        RefreshToken refreshToken = refreshTokenRepository.save(RefreshToken.builder()
                .user(user)
                .tokenHash("hash-refresh-123")
                .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .build());

        PasswordResetToken resetToken = passwordResetTokenRepository.save(PasswordResetToken.builder()
                .user(user)
                .tokenHash("hash-reset-123")
                .expiresAt(Instant.now().plus(30, ChronoUnit.MINUTES))
                .build());

        assertThat(refreshTokenRepository.findByTokenHash("hash-refresh-123")).isPresent();
        assertThat(passwordResetTokenRepository.findByTokenHash("hash-reset-123")).isPresent();
        assertThat(refreshToken.getId()).isNotNull();
        assertThat(resetToken.getId()).isNotNull();
    }

    @Test
    void shouldAllowDuplicateCategoryNameOnlyWhenPreviousIsSoftDeleted() {
        User user = userRepository.save(User.builder()
                .name("João Souza")
                .email("joao@example.com")
                .passwordHash("$argon2id$hash")
                .build());

        Category deletedCategory = categoryRepository.saveAndFlush(Category.builder()
                .user(user)
                .name("Trabalho")
                .deletedAt(Instant.now())
                .build());

        Category activeCategory = categoryRepository.saveAndFlush(Category.builder()
                .user(user)
                .name("Trabalho")
                .color("#10B981")
                .build());

        assertThat(deletedCategory.getId()).isNotEqualTo(activeCategory.getId());

        Category duplicateActive = Category.builder()
                .user(user)
                .name("trabalho")
                .build();

        assertThatThrownBy(() -> categoryRepository.saveAndFlush(duplicateActive))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldClearCategoryFromTasksWhenCategoryIsDeleted() {
        User user = userRepository.save(User.builder()
                .name("Ana Costa")
                .email("ana@example.com")
                .passwordHash("$argon2id$hash")
                .build());

        Category category = categoryRepository.save(Category.builder()
                .user(user)
                .name("Pessoal")
                .build());

        Task activeTask = taskRepository.save(Task.builder()
                .user(user)
                .category(category)
                .title("Tarefa ativa")
                .build());

        Task trashedTask = taskRepository.save(Task.builder()
                .user(user)
                .category(category)
                .title("Tarefa na lixeira")
                .deletedAt(Instant.now())
                .build());

        int updated = taskRepository.clearCategoryFromUserTasks(user.getId(), category.getId(), Instant.now());
        assertThat(updated).isEqualTo(2);

        assertThat(taskRepository.findByIdAndUserIdAndDeletedAtIsNull(activeTask.getId(), user.getId()))
                .get()
                .extracting(Task::getCategory)
                .isNull();
        assertThat(taskRepository.findByIdAndUserIdAndDeletedAtIsNotNull(trashedTask.getId(), user.getId()))
                .get()
                .extracting(Task::getCategory)
                .isNull();
    }
}
