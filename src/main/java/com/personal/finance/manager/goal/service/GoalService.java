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
import com.personal.finance.manager.category.entity.Category;
import com.personal.finance.manager.category.entity.CategoryType;
import com.personal.finance.manager.category.repository.CategoryRepository;
import com.personal.finance.manager.transaction.entity.Transaction;
import com.personal.finance.manager.transaction.entity.TransactionType;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoalService {

    private final GoalRepository goalRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public GoalResponse createGoal(Long userId, GoalRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Goal goal = Goal.builder()
                .goalName(request.getGoalName())
                .targetAmount(request.getTargetAmount())
                .startDate(request.getStartDate())
                .endDate(request.getTargetDate()) // mapping targetDate from DTO to endDate in Entity
                .user(user)
                .currentProgress(BigDecimal.ZERO)
                .isAchieved(false)
                .build();

        goal = goalRepository.save(goal);
        return mapToResponse(goal, user);
    }

    @Transactional(readOnly = true)
    public List<GoalResponse> getGoals(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return goalRepository.findByUser(user).stream()
                .map(goal -> mapToResponse(goal, user))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public GoalResponse getGoal(Long userId, Long goalId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Goal goal = goalRepository.findByIdAndUser(goalId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));

        return mapToResponse(goal, user);
    }

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

    @Transactional
    public void deleteGoal(Long userId, Long goalId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Goal goal = goalRepository.findByIdAndUser(goalId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));

        if (goal.getCurrentProgress() != null && goal.getCurrentProgress().compareTo(BigDecimal.ZERO) > 0) {
            Category incomeCategory = categoryRepository.findAll().stream()
                    .filter(c -> "Goal".equalsIgnoreCase(c.getName()) && c.getType() == CategoryType.INCOME && 
                            (c.getUser() == null || c.getUser().getId().equals(userId)))
                    .findFirst()
                    .orElseGet(() -> {
                        Category newCat = Category.builder()
                                .name("Goal")
                                .type(CategoryType.INCOME)
                                .user(user)
                                .build();
                        return categoryRepository.save(newCat);
                    });

            Transaction refund = Transaction.builder()
                    .amount(goal.getCurrentProgress())
                    .type(TransactionType.INCOME)
                    .category(incomeCategory)
                    .description("Income from goal: " + goal.getGoalName())
                    .date(LocalDate.now())
                    .user(user)
                    .build();

            transactionRepository.save(refund);
        }

        goalRepository.delete(goal);
    }

    @Transactional
    public GoalResponse addFunds(Long userId, Long goalId, BigDecimal amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Goal goal = goalRepository.findByIdAndUser(goalId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Fund amount must be greater than zero");
        }

        BigDecimal current = goal.getCurrentProgress() != null ? goal.getCurrentProgress() : BigDecimal.ZERO;
        BigDecimal remainingAmount = goal.getTargetAmount().subtract(current);

        if (amount.compareTo(remainingAmount) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot add more funds than the remaining amount of ₹" + remainingAmount);
        }

        BigDecimal newProgress = current.add(amount);
        goal.setCurrentProgress(newProgress);

        if (newProgress.compareTo(goal.getTargetAmount()) >= 0) {
            goal.setIsAchieved(true);
        }

        return mapToResponse(goalRepository.save(goal), user);
    }

    private GoalResponse mapToResponse(Goal goal, User user) {
        BigDecimal currentProgress = goal.getCurrentProgress();
        if (currentProgress == null) {
            currentProgress = BigDecimal.ZERO;
        }

        BigDecimal remainingAmount = goal.getTargetAmount().subtract(currentProgress);
        if (remainingAmount.compareTo(BigDecimal.ZERO) < 0) {
            remainingAmount = BigDecimal.ZERO;
        }

        double progressPercentage = 0.0;
        if (goal.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
            progressPercentage = currentProgress.divide(goal.getTargetAmount(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100")).doubleValue();
        }

        Boolean isAchieved = goal.getIsAchieved() != null ? goal.getIsAchieved() : false;

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
