package com.taskflow.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.taskflow.entity.ActivityLog;
import com.taskflow.enums.ActivityAction;
import com.taskflow.repository.ActivityLogRepository;
import com.taskflow.repository.CategoryRepository;
import com.taskflow.repository.PasswordResetTokenRepository;
import com.taskflow.repository.RefreshTokenRepository;
import com.taskflow.repository.TaskRepository;
import com.taskflow.repository.UserRepository;
import com.taskflow.security.LoginRateLimiter;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class TaskAndCategoryIntegrationTest {

    @Container
    @ServiceConnection
    static MySQLContainer mysql = new MySQLContainer("mysql:8.4");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ActivityLogRepository activityLogRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LoginRateLimiter loginRateLimiter;

    @BeforeEach
    void setUp() {
        loginRateLimiter.clearAll();
        activityLogRepository.deleteAll();
        taskRepository.deleteAll();
        categoryRepository.deleteAll();
        passwordResetTokenRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldCreateUpdateListSoftDeleteAndRestoreCategoryWithTaskUnlinking() throws Exception {
        String token = registerAndLogin("Ana Silva", "ana@taskflow.dev", "SenhaForte@123");

        // Invalid color #RRGGBB
        mockMvc.perform(post("/api/v1/categories")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Trabalho",
                                  "color": "azul"
                                }
                                """))
                .andExpect(status().isBadRequest());

        // Create category
        MvcResult createCat = mockMvc.perform(post("/api/v1/categories")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "  Trabalho  ",
                                  "color": "#3b82f6"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Trabalho"))
                .andExpect(jsonPath("$.color").value("#3b82f6"))
                .andReturn();

        long categoryId = readJson(createCat).get("id").asLong();

        // Duplicate normalized active name -> 409
        mockMvc.perform(post("/api/v1/categories")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "trabalho",
                                  "color": "#10B981"
                                }
                                """))
                .andExpect(status().isConflict());

        // Create two tasks linked to category (one active, one later trashed)
        long activeTaskId = createTask(token, """
                {
                  "title": "Revisar PR",
                  "categoryId": %d
                }
                """.formatted(categoryId));

        long trashedTaskId = createTask(token, """
                {
                  "title": "Arquivar docs",
                  "categoryId": %d
                }
                """.formatted(categoryId));

        // List categories shows taskCount = 2
        mockMvc.perform(get("/api/v1/categories")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(categoryId))
                .andExpect(jsonPath("$.content[0].taskCount").value(2));

        // Trash second task -> taskCount becomes 1
        mockMvc.perform(delete("/api/v1/tasks/{id}", trashedTaskId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/categories")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].taskCount").value(1));

        // Update category
        mockMvc.perform(patch("/api/v1/categories/{id}", categoryId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Corporativo",
                                  "color": "#10b981"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Corporativo"))
                .andExpect(jsonPath("$.color").value("#10b981"));

        // Soft delete category -> unlinks both active and trashed tasks
        mockMvc.perform(delete("/api/v1/categories/{id}", categoryId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/tasks/{id}", activeTaskId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").doesNotExist());

        mockMvc.perform(get("/api/v1/tasks")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .param("deleted", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(trashedTaskId))
                .andExpect(jsonPath("$.content[0].category").doesNotExist());

        // Restore category succeeds when no active category has same name
        mockMvc.perform(post("/api/v1/categories/{id}/restore", categoryId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Corporativo"))
                .andExpect(jsonPath("$.deletedAt").doesNotExist());

        // Delete again, create another category with same name, restore must fail with 409
        mockMvc.perform(delete("/api/v1/categories/{id}", categoryId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/v1/categories")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "corporativo",
                                  "color": "#6366F1"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/categories/{id}/restore", categoryId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldHandleTaskCrudPatchExplicitNullCategoryAndStatusTransitions() throws Exception {
        String token = registerAndLogin("Bruno Costa", "bruno@taskflow.dev", "SenhaForte@123");

        long categoryId = createCategory(token, "Backend", "#EF4444");

        // Create task with defaults (MEDIUM, PENDING)
        MvcResult createResult = mockMvc.perform(post("/api/v1/tasks")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Implementar endpoint",
                                  "description": "Detalhes iniciais",
                                  "categoryId": %d
                                }
                                """.formatted(categoryId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Implementar endpoint"))
                .andExpect(jsonPath("$.priority").value("MEDIUM"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.category.id").value(categoryId))
                .andExpect(jsonPath("$.overdue").value(false))
                .andReturn();

        long taskId = readJson(createResult).get("id").asLong();

        // PATCH omitting categoryId keeps existing category
        mockMvc.perform(patch("/api/v1/tasks/{id}", taskId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Implementar endpoint v2",
                                  "priority": "HIGH"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Implementar endpoint v2"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.category.id").value(categoryId));

        // PATCH with explicit "categoryId": null removes category
        mockMvc.perform(patch("/api/v1/tasks/{id}", taskId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoryId": null
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").doesNotExist());

        // PENDING -> IN_PROGRESS
        mockMvc.perform(patch("/api/v1/tasks/{id}/status", taskId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "IN_PROGRESS"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.completedAt").doesNotExist());

        // IN_PROGRESS -> COMPLETED sets completedAt
        mockMvc.perform(patch("/api/v1/tasks/{id}/status", taskId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "COMPLETED"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.completedAt").isNotEmpty());

        // Invalid transition COMPLETED -> CANCELLED -> 400 INVALID_STATUS_TRANSITION
        mockMvc.perform(patch("/api/v1/tasks/{id}/status", taskId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "CANCELLED"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_STATUS_TRANSITION"));

        // Reopen COMPLETED -> PENDING clears completedAt
        mockMvc.perform(patch("/api/v1/tasks/{id}/status", taskId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "PENDING"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.completedAt").doesNotExist());

        // PENDING -> CANCELLED
        mockMvc.perform(patch("/api/v1/tasks/{id}/status", taskId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "CANCELLED"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void shouldFilterPaginateSortAndManageTrashForTasks() throws Exception {
        String token = registerAndLogin("Clara Lima", "clara@taskflow.dev", "SenhaForte@123");

        long catWork = createCategory(token, "Trabalho", "#3B82F6");
        long catStudy = createCategory(token, "Estudos", "#10B981");

        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Instant yesterday = now.minus(1, ChronoUnit.DAYS);
        Instant tomorrow = now.plus(1, ChronoUnit.DAYS);
        Instant nextWeek = now.plus(7, ChronoUnit.DAYS);

        long t1 = createTask(token, """
                {
                  "title": "Relatório mensal",
                  "description": "Fechar indicadores",
                  "priority": "URGENT",
                  "categoryId": %d,
                  "dueDate": "%s"
                }
                """.formatted(catWork, yesterday));

        long t2 = createTask(token, """
                {
                  "title": "Estudar Spring",
                  "description": "Preparar relatório técnico",
                  "priority": "HIGH",
                  "categoryId": %d,
                  "dueDate": "%s"
                }
                """.formatted(catStudy, tomorrow));

        createTask(token, """
                {
                  "title": "Ler artigo",
                  "priority": "LOW",
                  "categoryId": %d,
                  "dueDate": "%s"
                }
                """.formatted(catStudy, nextWeek));

        // Combined filter: search + category + priority + date range
        mockMvc.perform(get("/api/v1/tasks")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .param("search", "relatório")
                        .param("categoryId", String.valueOf(catWork))
                        .param("priority", "URGENT")
                        .param("startDate", yesterday.minus(1, ChronoUnit.HOURS).toString())
                        .param("endDate", now.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(t1))
                .andExpect(jsonPath("$.content[0].overdue").value(true));

        // Pagination cap at 100 and sort
        mockMvc.perform(get("/api/v1/tasks")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .param("page", "0")
                        .param("size", "250")
                        .param("sort", "title,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(100))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.content[0].title").value("Estudar Spring"));

        // Soft delete t2 -> excluded from active, visible in deleted=true
        mockMvc.perform(delete("/api/v1/tasks/{id}", t2)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/tasks")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));

        mockMvc.perform(get("/api/v1/tasks")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .param("deleted", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(t2));

        // Restore t2
        mockMvc.perform(post("/api/v1/tasks/{id}/restore", t2)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(t2))
                .andExpect(jsonPath("$.deletedAt").doesNotExist());

        mockMvc.perform(get("/api/v1/tasks")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    void shouldEnforceMultiUserIsolationAndRecordActivityLogs() throws Exception {
        String tokenA = registerAndLogin("User A", "usera@taskflow.dev", "SenhaForte@123");
        String tokenB = registerAndLogin("User B", "userb@taskflow.dev", "SenhaForte@123");

        long catA = createCategory(tokenA, "Pessoal", "#8B5CF6");
        long taskA = createTask(tokenA, """
                {
                  "title": "Tarefa privada",
                  "categoryId": %d
                }
                """.formatted(catA));

        // User B cannot access, update, delete or restore User A's category or task (404)
        mockMvc.perform(get("/api/v1/categories/{id}", catA)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenB))
                .andExpect(status().isNotFound());

        mockMvc.perform(patch("/api/v1/categories/{id}", catA)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Invadida"
                                }
                                """))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/v1/categories/{id}", catA)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenB))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/v1/tasks/{id}", taskA)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenB))
                .andExpect(status().isNotFound());

        mockMvc.perform(patch("/api/v1/tasks/{id}", taskA)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Alterada"
                                }
                                """))
                .andExpect(status().isNotFound());

        mockMvc.perform(patch("/api/v1/tasks/{id}/status", taskA)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "COMPLETED"
                                }
                                """))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/v1/tasks/{id}", taskA)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenB))
                .andExpect(status().isNotFound());

        // User B cannot create a task linking User A's category
        mockMvc.perform(post("/api/v1/tasks")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Usando categoria alheia",
                                  "categoryId": %d
                                }
                                """.formatted(catA)))
                .andExpect(status().isNotFound());

        // Soft delete and restore by User A to generate ActivityLogs
        mockMvc.perform(delete("/api/v1/tasks/{id}", taskA)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenA))
                .andExpect(status().isNoContent());

        // User B cannot restore User A's trashed task
        mockMvc.perform(post("/api/v1/tasks/{id}/restore", taskA)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenB))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/v1/tasks/{id}/restore", taskA)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenA))
                .andExpect(status().isOk());

        long userAId = userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull("usera@taskflow.dev")
                .orElseThrow()
                .getId();
        List<ActivityAction> actions = activityLogRepository.findTop10ByUserIdOrderByCreatedAtDesc(userAId)
                .stream()
                .map(ActivityLog::getAction)
                .toList();

        assertThat(actions).containsExactlyInAnyOrder(
                ActivityAction.CATEGORY_CREATED,
                ActivityAction.TASK_CREATED,
                ActivityAction.TASK_DELETED,
                ActivityAction.TASK_RESTORED
        );
    }

    private String registerAndLogin(String name, String email, String password) throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(name, email, password)))
                .andExpect(status().isCreated());

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn();

        return readJson(loginResult).get("accessToken").asText();
    }

    private long createCategory(String token, String name, String color) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/categories")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "color": "%s"
                                }
                                """.formatted(name, color)))
                .andExpect(status().isCreated())
                .andReturn();
        return readJson(result).get("id").asLong();
    }

    private long createTask(String token, String jsonBody) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/tasks")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isCreated())
                .andReturn();
        return readJson(result).get("id").asLong();
    }

    private JsonNode readJson(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }
}
