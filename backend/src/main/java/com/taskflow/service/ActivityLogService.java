package com.taskflow.service;

import com.taskflow.entity.ActivityLog;
import com.taskflow.entity.Task;
import com.taskflow.entity.User;
import com.taskflow.enums.ActivityAction;
import com.taskflow.repository.ActivityLogRepository;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serviço compartilhado para registro de eventos no histórico de atividades.
 */
@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    @Transactional(propagation = Propagation.MANDATORY)
    public void record(
            User user,
            Task task,
            ActivityAction action,
            String description,
            Map<String, Object> metadata) {
        ActivityLog logEntry = ActivityLog.builder()
                .user(user)
                .task(task)
                .action(action)
                .description(description)
                .metadata(metadata)
                .build();
        activityLogRepository.save(logEntry);
    }
}
