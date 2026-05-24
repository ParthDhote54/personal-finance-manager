package com.personal.finance.manager.category.repository;

import com.personal.finance.manager.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Data access layer for Category entities.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByNameAndUserId(String name, Long userId);
    
    // Finds all default global categories
    List<Category> findByUserIdIsNull();
    
    boolean existsByNameAndUserId(String name, Long userId);
    
    boolean existsByNameAndUserIdIsNull(String name);

    Optional<Category> findByNameAndUserIdIsNull(String name);

    @Query("SELECT c FROM Category c WHERE c.id = :id AND (c.user.id = :userId OR c.user IS NULL)")
    Optional<Category> findByIdAndUserIdOrUserIdNull(@Param("id") Long id, @Param("userId") Long userId);

    @Query("SELECT c FROM Category c WHERE c.name = :name AND (c.user.id = :userId OR c.user IS NULL)")
    Optional<Category> findByNameAndUserIdOrUserIdNull(@Param("name") String name, @Param("userId") Long userId);
}
