package com.taskflow.mapper;

import com.taskflow.dto.auth.UserResponse;
import com.taskflow.entity.User;
import com.taskflow.exception.BadRequestException;
import java.time.DateTimeException;
import java.time.ZoneId;
import org.springframework.stereotype.Component;

/**
 * Conversor de entidades de usuário para DTOs e validador de fuso horário IANA.
 */
@Component
public class UserMapper {

    public static final String DEFAULT_TIMEZONE = "America/Sao_Paulo";

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getTimezone(),
                user.getCreatedAt()
        );
    }

    public String resolveAndValidateTimezone(String timezone) {
        if (timezone == null || timezone.isBlank()) {
            return DEFAULT_TIMEZONE;
        }
        String trimmed = timezone.trim();
        try {
            ZoneId.of(trimmed);
            return trimmed;
        } catch (DateTimeException ex) {
            throw new BadRequestException("INVALID_TIMEZONE", "Timezone must be a valid IANA identifier.");
        }
    }
}
