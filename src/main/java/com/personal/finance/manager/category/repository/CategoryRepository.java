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

    /**
     * Finds a custom category by name owned by a specific user.
     */
    Optional<Category> findByNameAndUserId(String name, Long userId);

    /**
     * Finds all global default categories (where user is null).
     */
    List<Category> findByUserIdIsNull();

    /**
     * Finds all custom categories belonging to a specific user.
     */
    List<Category> findByUserId(Long userId);

    /**
     * Checks if a custom category with the given name exists for a user.
     */
    boolean existsByNameAndUserId(String name, Long userId);

    /**
     * Checks if a global default category exists with the given name.
     */
    boolean existsByNameAndUserIdIsNull(String name);

    /**
     * Finds a global default category by name.
     */
    Optional<Category> findByNameAndUserIdIsNull(String name);

    /**
     * Finds a category by ID matching either the user's custom category or a global default category.
     */
    @Query("SELECT c FROM Category c WHERE c.id = :id AND (c.user.id = :userId OR c.user IS NULL)")
    Optional<Category> findByIdAndUserIdOrUserIdNull(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * Finds a category by name matching either the user's custom category or a global default category.
     */
    @Query("SELECT c FROM Category c WHERE c.name = :name AND (c.user.id = :userId OR c.user IS NULL)")
    Optional<Category> findByNameAndUserIdOrUserIdNull(@Param("name") String name, @Param("userId") Long userId);
}
