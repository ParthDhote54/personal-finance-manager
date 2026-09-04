package com.personal.finance.manager.user.repository;

import com.personal.finance.manager.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Data access layer for User entities.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user account by username.
     */
    Optional<User> findByUsername(String username);

    /**
     * Checks if a user account exists with the specified username.
     */
    boolean existsByUsername(String username);
}
