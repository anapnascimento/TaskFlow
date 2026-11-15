package com.taskflow.repository;

import com.taskflow.entity.Task;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repositório de persistência para tarefas com suporte a filtros dinâmicos e isolamento por usuário.
 */
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    Optional<Task> findByIdAndUserIdAndDeletedAtIsNull(Long id, Long userId);

    Optional<Task> findByIdAndUserIdAndDeletedAtIsNotNull(Long id, Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Task t SET t.category = null, t.updatedAt = :updatedAt WHERE t.user.id = :userId AND t.category.id = :categoryId")
    int clearCategoryFromUserTasks(
            @Param("userId") Long userId,
            @Param("categoryId") Long categoryId,
            @Param("updatedAt") Instant updatedAt);
}
