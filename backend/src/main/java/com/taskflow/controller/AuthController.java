package com.taskflow.controller;

import com.taskflow.dto.auth.ForgotPasswordRequest;
import com.taskflow.dto.auth.LoginRequest;
import com.taskflow.dto.auth.LoginResponse;
import com.taskflow.dto.auth.MessageResponse;
import com.taskflow.dto.auth.RefreshResponse;
import com.taskflow.dto.auth.RegisterRequest;
import com.taskflow.dto.auth.ResetPasswordRequest;
import com.taskflow.dto.auth.UserResponse;
import com.taskflow.service.AuthService;
import com.taskflow.service.AuthService.AuthTokensResult;
import com.taskflow.service.AuthService.RefreshTokensResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller REST para cadastro, autenticação, renovação de sessão, logout e recuperação de senha.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    public static final String REFRESH_COOKIE_NAME = "refresh_token";

    private final AuthService authService;

    @Value("${app.jwt.refresh-expiration-days}")
    private long refreshExpirationDays;

    @Value("${app.cookie.secure}")
    private boolean cookieSecure;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        String clientIp = extractClientIp(httpRequest);
        AuthTokensResult result = authService.login(request, clientIp);
        ResponseCookie cookie = buildRefreshCookie(result.rawRefreshToken(), Duration.ofDays(refreshExpirationDays));
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(result.response());
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(
            @CookieValue(name = REFRESH_COOKIE_NAME, required = false) String refreshToken) {
        RefreshTokensResult result = authService.refresh(refreshToken);
        ResponseCookie cookie = buildRefreshCookie(result.rawRefreshToken(), Duration.ofDays(refreshExpirationDays));
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(result.response());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = REFRESH_COOKIE_NAME, required = false) String refreshToken) {
        authService.logout(refreshToken);
        ResponseCookie clearCookie = buildRefreshCookie("", Duration.ZERO);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, clearCookie.toString())
                .build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me() {
        return ResponseEntity.ok(authService.me());
    }

    @PostMapping("/password/forgot")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(authService.forgotPassword(request));
    }

    @PostMapping("/password/reset")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(authService.resetPassword(request));
    }

    private ResponseCookie buildRefreshCookie(String value, Duration maxAge) {
        return ResponseCookie.from(REFRESH_COOKIE_NAME, value)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/api/v1/auth")
                .maxAge(maxAge)
                .build();
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
