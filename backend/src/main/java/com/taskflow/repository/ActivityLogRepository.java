package com.taskflow.repository;

import com.taskflow.entity.ActivityLog;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositório de persistência para registros do histórico de atividades.
 */
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    Page<ActivityLog> findAllByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<ActivityLog> findTop10ByUserIdOrderByCreatedAtDesc(Long userId);
}
