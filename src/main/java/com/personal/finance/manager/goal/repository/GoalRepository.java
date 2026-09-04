package com.personal.finance.manager.goal.repository;

import com.personal.finance.manager.goal.entity.Goal;
import com.personal.finance.manager.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing Goal database entities.
 */
@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {

    /**
     * Finds all savings goals belonging to a specific user.
     */
    List<Goal> findByUser(User user);

    /**
     * Finds a savings goal by ID owned by a specific user.
     */
    Optional<Goal> findByIdAndUser(Long id, User user);
}
