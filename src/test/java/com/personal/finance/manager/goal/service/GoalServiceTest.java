package com.personal.finance.manager.goal.service;

import com.personal.finance.manager.goal.dto.GoalResponse;
import com.personal.finance.manager.goal.entity.Goal;
import com.personal.finance.manager.goal.repository.GoalRepository;
import com.personal.finance.manager.transaction.repository.TransactionRepository;
import com.personal.finance.manager.user.entity.User;
import com.personal.finance.manager.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class GoalServiceTest {

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private GoalService goalService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGoalMathWithNegativeNetSavings() {
        User user = new User();
        user.setId(1L);

        Goal goal = Goal.builder()
                .id(1L)
                .targetAmount(new BigDecimal("5000.00"))
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2026, 1, 1))
                .user(user)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(goalRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(goal));

        // Expenses are greater than income -> Net Savings is negative
        when(transactionRepository.sumNetTransactionsBetweenDates(user, goal.getStartDate(), goal.getEndDate()))
                .thenReturn(new BigDecimal("-500.00"));

        GoalResponse response = goalService.getGoal(1L, 1L);

        // Negative progress should snap to ZERO or remain negative depending on assignment, but prompt asks to handle "negative net savings".
        // In my logic, I snapped currentProgress to 0 to prevent negative progress throwing off percentages.
        assertEquals(new BigDecimal("0"), response.getCurrentProgress());
        assertEquals(new BigDecimal("5000.00"), response.getRemainingAmount()); // Full amount remaining
        assertEquals(0.0, response.getProgressPercentage());
    }

    @Test
    public void testGoalMathWithZeroTargetAmount() {
        User user = new User();
        user.setId(1L);

        Goal goal = Goal.builder()
                .id(1L)
                .targetAmount(BigDecimal.ZERO)
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2026, 1, 1))
                .user(user)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(goalRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(goal));
        when(transactionRepository.sumNetTransactionsBetweenDates(user, goal.getStartDate(), goal.getEndDate()))
                .thenReturn(new BigDecimal("1000.00"));

        GoalResponse response = goalService.getGoal(1L, 1L);

        assertEquals(0.0, response.getProgressPercentage()); // Should not throw DivideByZero
        assertEquals(BigDecimal.ZERO, response.getRemainingAmount()); // Snapped to zero instead of negative
    }

    @Test
    public void testGoalMathWithStablePercentage() {
        User user = new User();
        user.setId(1L);

        Goal goal = Goal.builder()
                .id(1L)
                .targetAmount(new BigDecimal("3333.33"))
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2026, 1, 1))
                .user(user)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(goalRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(goal));
        when(transactionRepository.sumNetTransactionsBetweenDates(user, goal.getStartDate(), goal.getEndDate()))
                .thenReturn(new BigDecimal("1111.11"));

        GoalResponse response = goalService.getGoal(1L, 1L);

        // 1111.11 / 3333.33 = ~0.3333 * 100 = 33.33%
        assertEquals(33.33, response.getProgressPercentage());
    }
}
