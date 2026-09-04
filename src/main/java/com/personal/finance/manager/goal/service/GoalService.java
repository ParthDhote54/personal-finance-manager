package com.personal.finance.manager.goal.service;

import com.personal.finance.manager.exception.ResourceNotFoundException;
import com.personal.finance.manager.goal.dto.GoalRequest;
import com.personal.finance.manager.goal.dto.GoalResponse;
import com.personal.finance.manager.goal.entity.Goal;
import com.personal.finance.manager.goal.repository.GoalRepository;
import com.personal.finance.manager.transaction.repository.TransactionRepository;
import com.personal.finance.manager.user.entity.User;
import com.personal.finance.manager.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service managing financial savings goal creation, retrieval, updates, dynamic progress tracking, and deletion.
 */
@Service
@RequiredArgsConstructor
public class GoalService {

    private final GoalRepository goalRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    /**
     * Creates a new savings goal for the specified user.
     *
     * @param userId  ID of the authenticated user
     * @param request goal creation request payload
     * @return created goal details with dynamic progress
     */
    @Transactional
    public GoalResponse createGoal(Long userId, GoalRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // startDate defaults to creation date if not provided
        LocalDate startDate = request.getStartDate() != null ? request.getStartDate() : LocalDate.now();

        Goal goal = Goal.builder()
                .goalName(request.getGoalName())
                .targetAmount(request.getTargetAmount())
                .startDate(startDate)
                .endDate(request.getTargetDate()) // mapping targetDate from DTO to endDate in Entity
                .user(user)
                .isAchieved(false)
                .build();

        goal = goalRepository.save(goal);
        return mapToResponse(goal, user);
    }

    /**
     * Retrieves all savings goals owned by the specified user with updated dynamic progress metrics.
     *
     * @param userId ID of the authenticated user
     * @return list of goal responses
     */
    @Transactional(readOnly = true)
    public List<GoalResponse> getGoals(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return goalRepository.findByUser(user).stream()
                .map(goal -> mapToResponse(goal, user))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a specific savings goal owned by the specified user.
     *
     * @param userId ID of the authenticated user
     * @param goalId ID of the goal to retrieve
     * @return goal response object
     */
    @Transactional(readOnly = true)
    public GoalResponse getGoal(Long userId, Long goalId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Goal goal = goalRepository.findByIdAndUser(goalId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));

        return mapToResponse(goal, user);
    }

    /**
     * Updates target amount and target date for an existing savings goal owned by the user.
     *
     * @param userId  ID of the authenticated user
     * @param goalId  ID of the goal to update
     * @param request update payload containing new target amount/date
     * @return updated goal response
     */
    @Transactional
    public GoalResponse updateGoal(Long userId, Long goalId, GoalRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Goal goal = goalRepository.findByIdAndUser(goalId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));

        goal.setTargetAmount(request.getTargetAmount());
        goal.setEndDate(request.getTargetDate());
        
        return mapToResponse(goalRepository.save(goal), user);
    }

    /**
     * Deletes a savings goal owned by the specified user.
     *
     * @param userId ID of the authenticated user
     * @param goalId ID of the goal to delete
     */
    @Transactional
    public void deleteGoal(Long userId, Long goalId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Goal goal = goalRepository.findByIdAndUser(goalId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));

        goalRepository.delete(goal);
    }

    /**
     * Calculates goal progress dynamically from transactions.
     * Progress = Total Income - Total Expenses since the goal's start date.
     */
    private GoalResponse mapToResponse(Goal goal, User user) {
        // Dynamically calculate progress from transactions since goal start date
        BigDecimal currentProgress = transactionRepository.sumNetTransactionsBetweenDates(
                user, goal.getStartDate(), LocalDate.now());
        if (currentProgress == null) {
            currentProgress = BigDecimal.ZERO;
        }

        BigDecimal targetAmount = goal.getTargetAmount() != null ? goal.getTargetAmount() : BigDecimal.ZERO;
        BigDecimal remainingAmount = targetAmount.subtract(currentProgress);
        if (remainingAmount.compareTo(BigDecimal.ZERO) < 0) {
            remainingAmount = BigDecimal.ZERO;
        }

        double progressPercentage = 0.0;
        if (targetAmount.compareTo(BigDecimal.ZERO) > 0) {
            progressPercentage = currentProgress.divide(targetAmount, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100")).doubleValue();
        }

        boolean isAchieved = currentProgress.compareTo(targetAmount) >= 0;

        return GoalResponse.builder()
                .id(goal.getId())
                .goalName(goal.getGoalName())
                .targetAmount(goal.getTargetAmount())
                .targetDate(goal.getEndDate())
                .startDate(goal.getStartDate())
                .currentProgress(currentProgress)
                .progressPercentage(progressPercentage)
                .remainingAmount(remainingAmount)
                .isAchieved(isAchieved)
                .build();
    }
}
