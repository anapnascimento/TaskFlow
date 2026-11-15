package com.taskflow.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.taskflow.entity.PasswordResetToken;
import com.taskflow.entity.User;
import com.taskflow.repository.PasswordResetTokenRepository;
import com.taskflow.repository.RefreshTokenRepository;
import com.taskflow.repository.UserRepository;
import com.taskflow.security.LoginRateLimiter;
import com.taskflow.security.TokenHashUtil;
import jakarta.servlet.http.Cookie;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
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
class AuthAndUserIntegrationTest {

    @Container
    @ServiceConnection
    static MySQLContainer mysql = new MySQLContainer("mysql:8.4");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private LoginRateLimiter loginRateLimiter;

    @BeforeEach
    void setUp() {
        loginRateLimiter.clearAll();
        passwordResetTokenRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldRegisterAndRejectDuplicateEmail() throws Exception {
        String body = """
                {
                  "name": "Alice Doe",
                  "email": "Alice@Example.com",
                  "password": "StrongPassword123!",
                  "timezone": "America/Sao_Paulo"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.timezone").value("America/Sao_Paulo"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EMAIL_ALREADY_EXISTS"));
    }

    @Test
    void shouldLoginRotateRefreshTokenAndDetectReuse() throws Exception {
        registerUser("bob@example.com", "StrongPassword123!");

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "bob@example.com",
                                  "password": "StrongPassword123!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("HttpOnly")))
                .andReturn();

        Cookie initialCookie = loginResult.getResponse().getCookie(AuthController.REFRESH_COOKIE_NAME);
        assertThat(initialCookie).isNotNull();

        JsonNode loginJson = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String accessToken = loginJson.get("accessToken").asString();

        mockMvc.perform(get("/api/v1/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("bob@example.com"));

        MvcResult refreshResult = mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(initialCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andReturn();

        Cookie rotatedCookie = refreshResult.getResponse().getCookie(AuthController.REFRESH_COOKIE_NAME);
        assertThat(rotatedCookie).isNotNull();
        assertThat(rotatedCookie.getValue()).isNotEqualTo(initialCookie.getValue());

        // Reusing the old rotated refresh token must fail and revoke the family
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(initialCookie))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("REFRESH_TOKEN_REUSED"));

        // The newer rotated cookie must now also be revoked
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(rotatedCookie))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldEnforceRateLimitingOnFailedLogins() throws Exception {
        registerUser("carol@example.com", "StrongPassword123!");

        String invalidPayload = """
                {
                  "email": "carol@example.com",
                  "password": "WrongPassword!"
                }
                """;

        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidPayload))
                    .andExpect(status().isUnauthorized());
        }

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("TOO_MANY_REQUESTS"));
    }

    @Test
    void shouldRejectUnauthenticatedAndInvalidTokens() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

        mockMvc.perform(get("/api/v1/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid.jwt.token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldLogoutUpdateProfileChangePasswordAndSoftDeleteAccount() throws Exception {
        registerUser("dave@example.com", "StrongPassword123!");

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "dave@example.com",
                                  "password": "StrongPassword123!"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        Cookie refreshCookie = loginResult.getResponse().getCookie(AuthController.REFRESH_COOKIE_NAME);
        String accessToken = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("accessToken").asString();

        mockMvc.perform(patch("/api/v1/users/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Dave Updated",
                                  "timezone": "UTC"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Dave Updated"))
                .andExpect(jsonPath("$.timezone").value("UTC"));

        mockMvc.perform(patch("/api/v1/users/me/password")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "currentPassword": "StrongPassword123!",
                                  "newPassword": "NewPassword456!"
                                }
                                """))
                .andExpect(status().isNoContent());

        // Password change revokes active refresh tokens
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(refreshCookie))
                .andExpect(status().isUnauthorized());

        // Login with new password
        MvcResult relogin = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "dave@example.com",
                                  "password": "NewPassword456!"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        String newAccessToken = objectMapper.readTree(relogin.getResponse().getContentAsString())
                .get("accessToken").asString();
        Cookie newRefreshCookie = relogin.getResponse().getCookie(AuthController.REFRESH_COOKIE_NAME);

        // Logout revokes session
        mockMvc.perform(post("/api/v1/auth/logout")
                        .cookie(newRefreshCookie))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(newRefreshCookie))
                .andExpect(status().isUnauthorized());

        // Soft delete account blocks future logins
        mockMvc.perform(delete("/api/v1/users/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + newAccessToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "dave@example.com",
                                  "password": "NewPassword456!"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldSupportForgotAndSingleUseResetPasswordFlow() throws Exception {
        User user = userRepository.save(User.builder()
                .name("Eva")
                .email("eva@example.com")
                .passwordHash(passwordEncoder.encode("OldPassword123!"))
                .build());

        mockMvc.perform(post("/api/v1/auth/password/forgot")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "eva@example.com"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("If the account exists, instructions will be sent."));

        String rawToken = "test-reset-token-123";
        passwordResetTokenRepository.save(PasswordResetToken.builder()
                .user(user)
                .tokenHash(TokenHashUtil.sha256Hex(rawToken))
                .expiresAt(Instant.now().plus(30, ChronoUnit.MINUTES))
                .build());

        mockMvc.perform(post("/api/v1/auth/password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "token": "test-reset-token-123",
                                  "newPassword": "ResetPassword999!"
                                }
                                """))
                .andExpect(status().isOk());

        // Reusing same reset token fails
        mockMvc.perform(post("/api/v1/auth/password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "token": "test-reset-token-123",
                                  "newPassword": "AnotherPassword999!"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_RESET_TOKEN"));

        // Login works with new password
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "eva@example.com",
                                  "password": "ResetPassword999!"
                                }
                                """))
                .andExpect(status().isOk());
    }

    private void registerUser(String email, String password) throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Test User",
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isCreated());
    }
}
