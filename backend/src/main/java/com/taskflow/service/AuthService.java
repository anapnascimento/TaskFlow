package com.taskflow.service;

import com.taskflow.dto.auth.ForgotPasswordRequest;
import com.taskflow.dto.auth.LoginRequest;
import com.taskflow.dto.auth.LoginResponse;
import com.taskflow.dto.auth.MessageResponse;
import com.taskflow.dto.auth.RefreshResponse;
import com.taskflow.dto.auth.RegisterRequest;
import com.taskflow.dto.auth.ResetPasswordRequest;
import com.taskflow.dto.auth.UserResponse;
import com.taskflow.entity.PasswordResetToken;
import com.taskflow.entity.RefreshToken;
import com.taskflow.entity.User;
import com.taskflow.exception.BadRequestException;
import com.taskflow.exception.ConflictException;
import com.taskflow.exception.UnauthorizedException;
import com.taskflow.mapper.UserMapper;
import com.taskflow.repository.PasswordResetTokenRepository;
import com.taskflow.repository.RefreshTokenRepository;
import com.taskflow.repository.UserRepository;
import com.taskflow.security.JwtService;
import com.taskflow.security.LoginRateLimiter;
import com.taskflow.security.SecurityUtils;
import com.taskflow.security.TokenHashUtil;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serviço responsável por cadastro, autenticação, rotação de Refresh Tokens,
 * logout e recuperação de senha.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final long PASSWORD_RESET_EXPIRATION_MINUTES = 30;
    private static final String BEARER_TYPE = "Bearer";

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LoginRateLimiter loginRateLimiter;
    private final UserMapper userMapper;
    private final Environment environment;

    @Value("${app.jwt.refresh-expiration-days}")
    private long refreshExpirationDays;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ConflictException("EMAIL_ALREADY_EXISTS", "Email is already registered.");
        }

        String timezone = userMapper.resolveAndValidateTimezone(request.timezone());
        User user = User.builder()
                .name(request.name().trim())
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(request.password()))
                .timezone(timezone)
                .build();

        User saved = userRepository.save(user);
        log.info("User registered successfully: userId={}", saved.getId());
        return userMapper.toResponse(saved);
    }

    @Transactional
    public AuthTokensResult login(LoginRequest request, String clientIp) {
        String normalizedEmail = normalizeEmail(request.email());
        String rateLimitKey = normalizedEmail + "|" + (clientIp != null ? clientIp : "unknown");
        loginRateLimiter.checkAllowed(rateLimitKey);

        User user = userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(normalizedEmail)
                .orElse(null);

        if (user == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            loginRateLimiter.recordFailure(rateLimitKey);
            log.warn("Failed login attempt for email={} ip={}", normalizedEmail, clientIp);
            throw new UnauthorizedException("INVALID_CREDENTIALS", "Invalid email or password.");
        }

        loginRateLimiter.clear(rateLimitKey);

        String accessToken = jwtService.generateAccessToken(user.getId());
        String rawRefreshToken = createRefreshTokenForUser(user);

        LoginResponse response = new LoginResponse(
                accessToken,
                BEARER_TYPE,
                jwtService.getAccessExpirationSeconds(),
                userMapper.toResponse(user)
        );
        log.info("User authenticated successfully: userId={}", user.getId());
        return new AuthTokensResult(response, rawRefreshToken);
    }

    @Transactional(noRollbackFor = UnauthorizedException.class)
    public RefreshTokensResult refresh(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new UnauthorizedException("INVALID_REFRESH_TOKEN", "Refresh token is missing.");
        }

        String tokenHash = TokenHashUtil.sha256Hex(rawRefreshToken);
        RefreshToken existing = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new UnauthorizedException("INVALID_REFRESH_TOKEN", "Invalid refresh token."));

        Instant now = Instant.now();

        if (existing.getReplacedBy() != null || existing.getRevokedAt() != null) {
            refreshTokenRepository.revokeAllActiveByUserId(existing.getUser().getId(), now);
            log.warn("Refresh token reuse or revoked token detected; all sessions revoked for userId={}",
                    existing.getUser().getId());
            throw new UnauthorizedException("REFRESH_TOKEN_REUSED", "Session revoked due to token reuse.");
        }

        if (existing.getExpiresAt().isBefore(now)) {
            existing.setRevokedAt(now);
            refreshTokenRepository.save(existing);
            throw new UnauthorizedException("REFRESH_TOKEN_EXPIRED", "Refresh token has expired.");
        }

        User user = existing.getUser();
        if (user.getDeletedAt() != null) {
            refreshTokenRepository.revokeAllActiveByUserId(user.getId(), now);
            throw new UnauthorizedException("ACCOUNT_DISABLED", "User account is no longer active.");
        }

        String newRawRefreshToken = TokenHashUtil.generateOpaqueToken();
        RefreshToken replacement = RefreshToken.builder()
                .user(user)
                .tokenHash(TokenHashUtil.sha256Hex(newRawRefreshToken))
                .expiresAt(now.plus(refreshExpirationDays, ChronoUnit.DAYS))
                .build();
        RefreshToken savedReplacement = refreshTokenRepository.save(replacement);

        existing.setRevokedAt(now);
        existing.setReplacedBy(savedReplacement);
        refreshTokenRepository.save(existing);

        String newAccessToken = jwtService.generateAccessToken(user.getId());
        RefreshResponse response = new RefreshResponse(
                newAccessToken,
                BEARER_TYPE,
                jwtService.getAccessExpirationSeconds()
        );
        return new RefreshTokensResult(response, newRawRefreshToken);
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            return;
        }
        String tokenHash = TokenHashUtil.sha256Hex(rawRefreshToken);
        refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(token -> {
            if (token.getRevokedAt() == null) {
                token.setRevokedAt(Instant.now());
                refreshTokenRepository.save(token);
                log.info("Session revoked on logout: userId={}", token.getUser().getId());
            }
        });
    }

    @Transactional(readOnly = true)
    public UserResponse me() {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new UnauthorizedException("UNAUTHORIZED", "Authenticated user not found."));
        return userMapper.toResponse(user);
    }

    @Transactional
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(normalizedEmail).ifPresent(user -> {
            String rawToken = TokenHashUtil.generateOpaqueToken();
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .user(user)
                    .tokenHash(TokenHashUtil.sha256Hex(rawToken))
                    .expiresAt(Instant.now().plus(PASSWORD_RESET_EXPIRATION_MINUTES, ChronoUnit.MINUTES))
                    .build();
            passwordResetTokenRepository.save(resetToken);

            if (environment.acceptsProfiles(Profiles.of("dev"))) {
                log.info("DEV PASSWORD RESET TOKEN for userId={}: token={}", user.getId(), rawToken);
            } else {
                log.info("Password reset requested for userId={}", user.getId());
            }
        });

        return new MessageResponse("If the account exists, instructions will be sent.");
    }

    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        String tokenHash = TokenHashUtil.sha256Hex(request.token().trim());
        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BadRequestException("INVALID_RESET_TOKEN", "Invalid or expired reset token."));

        Instant now = Instant.now();
        if (resetToken.getUsedAt() != null || resetToken.getExpiresAt().isBefore(now)) {
            throw new BadRequestException("INVALID_RESET_TOKEN", "Invalid or expired reset token.");
        }

        User user = resetToken.getUser();
        if (user.getDeletedAt() != null) {
            throw new BadRequestException("INVALID_RESET_TOKEN", "Invalid or expired reset token.");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        resetToken.setUsedAt(now);
        passwordResetTokenRepository.save(resetToken);

        refreshTokenRepository.revokeAllActiveByUserId(user.getId(), now);
        log.info("Password reset completed and sessions revoked for userId={}", user.getId());

        return new MessageResponse("Password has been reset successfully.");
    }

    private String createRefreshTokenForUser(User user) {
        String rawRefreshToken = TokenHashUtil.generateOpaqueToken();
        RefreshToken entity = RefreshToken.builder()
                .user(user)
                .tokenHash(TokenHashUtil.sha256Hex(rawRefreshToken))
                .expiresAt(Instant.now().plus(refreshExpirationDays, ChronoUnit.DAYS))
                .build();
        refreshTokenRepository.save(entity);
        return rawRefreshToken;
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    public record AuthTokensResult(LoginResponse response, String rawRefreshToken) {
    }

    public record RefreshTokensResult(RefreshResponse response, String rawRefreshToken) {
    }
}
