package com.taskflow.security;

import com.taskflow.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utilitário para obtenção da identidade do usuário autenticado no backend.
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new UnauthorizedException("UNAUTHORIZED", "Authentication is required.");
        }
        return user.id();
    }
}
