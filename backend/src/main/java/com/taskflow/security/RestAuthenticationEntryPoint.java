package com.taskflow.security;

import com.taskflow.exception.ProblemDetailResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * EntryPoint para retornar erro 401 padronizado em RFC 9457 quando a requisição não está autenticada.
 */
@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {
        ProblemDetailResponse body = new ProblemDetailResponse(
                "about:blank",
                "Unauthorized",
                HttpStatus.UNAUTHORIZED.value(),
                "Authentication is required to access this resource.",
                request.getRequestURI(),
                "UNAUTHORIZED",
                Instant.now(),
                null
        );
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
