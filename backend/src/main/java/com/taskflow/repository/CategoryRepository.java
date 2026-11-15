package com.taskflow.repository;

import com.taskflow.entity.Category;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repositório de persistência para categorias de tarefas.
 */
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findAllByUserIdAndDeletedAtIsNullOrderByNameAsc(Long userId);

    Optional<Category> findByIdAndUserIdAndDeletedAtIsNull(Long id, Long userId);

    Optional<Category> findByIdAndUserIdAndDeletedAtIsNotNull(Long id, Long userId);

    boolean existsByUserIdAndNameIgnoreCaseAndDeletedAtIsNull(Long userId, String name);

    boolean existsByUserIdAndNameIgnoreCaseAndDeletedAtIsNullAndIdNot(Long userId, String name, Long id);

    @Query("""
            SELECT t.category.id, COUNT(t)
            FROM Task t
            WHERE t.user.id = :userId
              AND t.deletedAt IS NULL
              AND t.category IS NOT NULL
            GROUP BY t.category.id
            """)
    List<Object[]> countActiveTasksByCategory(@Param("userId") Long userId);
}
