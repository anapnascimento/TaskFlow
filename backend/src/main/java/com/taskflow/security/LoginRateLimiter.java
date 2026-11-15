package com.taskflow.security;

import com.taskflow.exception.TooManyRequestsException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Limitador em memória para tentativas falhas de login (máximo 5 falhas em 15 minutos por e-mail/IP).
 */
@Component
public class LoginRateLimiter {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration WINDOW = Duration.ofMinutes(15);

    private final Map<String, AttemptWindow> attempts = new ConcurrentHashMap<>();

    public void checkAllowed(String key) {
        AttemptWindow window = attempts.get(key);
        if (window == null) {
            return;
        }
        Instant now = Instant.now();
        if (window.windowStart().plus(WINDOW).isBefore(now)) {
            attempts.remove(key);
            return;
        }
        if (window.failures() >= MAX_ATTEMPTS) {
            throw new TooManyRequestsException("Too many failed login attempts. Please try again later.");
        }
    }

    public void recordFailure(String key) {
        Instant now = Instant.now();
        attempts.compute(key, (k, existing) -> {
            if (existing == null || existing.windowStart().plus(WINDOW).isBefore(now)) {
                return new AttemptWindow(now, 1);
            }
            return new AttemptWindow(existing.windowStart(), existing.failures() + 1);
        });
    }

    public void clear(String key) {
        attempts.remove(key);
    }

    public void clearAll() {
        attempts.clear();
    }

    private record AttemptWindow(Instant windowStart, int failures) {
    }
}
