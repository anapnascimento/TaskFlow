package com.taskflow.service;

import com.taskflow.dto.auth.UserResponse;
import com.taskflow.dto.user.ChangePasswordRequest;
import com.taskflow.dto.user.UpdateProfileRequest;
import com.taskflow.entity.User;
import com.taskflow.exception.BadRequestException;
import com.taskflow.exception.UnauthorizedException;
import com.taskflow.mapper.UserMapper;
import com.taskflow.repository.RefreshTokenRepository;
import com.taskflow.repository.UserRepository;
import com.taskflow.security.SecurityUtils;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serviço responsável pelo gerenciamento de perfil, alteração de senha e exclusão lógica da conta.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Transactional
    public UserResponse updateProfile(UpdateProfileRequest request) {
        User user = getActiveCurrentUser();

        if (request.name() != null) {
            String trimmedName = request.name().trim();
            if (trimmedName.isEmpty()) {
                throw new BadRequestException("VALIDATION_ERROR", "Name cannot be blank.");
            }
            user.setName(trimmedName);
        }

        if (request.timezone() != null) {
            user.setTimezone(userMapper.resolveAndValidateTimezone(request.timezone()));
        }

        User saved = userRepository.save(user);
        return userMapper.toResponse(saved);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User user = getActiveCurrentUser();

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("INVALID_CURRENT_PASSWORD", "Current password is incorrect.");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        refreshTokenRepository.revokeAllActiveByUserId(user.getId(), Instant.now());
        log.info("Password changed and all sessions revoked for userId={}", user.getId());
    }

    @Transactional
    public void deleteAccount() {
        User user = getActiveCurrentUser();
        Instant now = Instant.now();
        user.setDeletedAt(now);
        userRepository.save(user);

        refreshTokenRepository.revokeAllActiveByUserId(user.getId(), now);
        log.info("User account soft-deleted and all sessions revoked: userId={}", user.getId());
    }

    private User getActiveCurrentUser() {
        Long userId = SecurityUtils.getCurrentUserId();
        return userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new UnauthorizedException("UNAUTHORIZED", "Authenticated user not found."));
    }
}
