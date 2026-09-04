package com.personal.finance.manager.goal.service;

import com.personal.finance.manager.goal.dto.GoalRequest;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    public void testGoalProgressCalculatedFromTransactions() {
        User user = new User();
        user.setId(1L);

        Goal goal = Goal.builder()
                .id(1L)
                .goalName("Emergency Fund")
                .targetAmount(new BigDecimal("5000.00"))
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2026, 1, 1))
                .user(user)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(goalRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(goal));
        // Simulate net income of 2000 (income - expenses since goal start date)
        when(transactionRepository.sumNetTransactionsBetweenDates(eq(user), eq(LocalDate.of(2025, 1, 1)), any(LocalDate.class)))
                .thenReturn(new BigDecimal("2000.00"));

        GoalResponse response = goalService.getGoal(1L, 1L);

        assertEquals(new BigDecimal("2000.00"), response.getCurrentProgress());
        assertEquals(new BigDecimal("3000.00"), response.getRemainingAmount());
        assertEquals(40.0, response.getProgressPercentage());
        assertFalse(response.getIsAchieved());
    }

    @Test
    public void testGoalProgressWithNegativeNetSavings() {
        User user = new User();
        user.setId(1L);

        Goal goal = Goal.builder()
                .id(1L)
                .goalName("Save Up")
                .targetAmount(new BigDecimal("5000.00"))
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2026, 1, 1))
                .user(user)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(goalRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(goal));
        // Negative net: more expenses than income
        when(transactionRepository.sumNetTransactionsBetweenDates(eq(user), eq(LocalDate.of(2025, 1, 1)), any(LocalDate.class)))
                .thenReturn(new BigDecimal("-500.00"));

        GoalResponse response = goalService.getGoal(1L, 1L);

        assertEquals(new BigDecimal("-500.00"), response.getCurrentProgress());
        assertEquals(new BigDecimal("5500.00"), response.getRemainingAmount()); // target + deficit
        assertFalse(response.getIsAchieved());
    }

    @Test
    public void testGoalProgressWithZeroTransactions() {
        User user = new User();
        user.setId(1L);

        Goal goal = Goal.builder()
                .id(1L)
                .goalName("New Goal")
                .targetAmount(new BigDecimal("3000.00"))
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2026, 1, 1))
                .user(user)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(goalRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(goal));
        when(transactionRepository.sumNetTransactionsBetweenDates(eq(user), eq(LocalDate.of(2025, 1, 1)), any(LocalDate.class)))
                .thenReturn(BigDecimal.ZERO);

        GoalResponse response = goalService.getGoal(1L, 1L);

        assertEquals(BigDecimal.ZERO, response.getCurrentProgress());
        assertEquals(new BigDecimal("3000.00"), response.getRemainingAmount());
        assertEquals(0.0, response.getProgressPercentage());
        assertFalse(response.getIsAchieved());
    }

    @Test
    public void testGoalAchievedWhenProgressExceedsTarget() {
        User user = new User();
        user.setId(1L);

        Goal goal = Goal.builder()
                .id(1L)
                .goalName("Short Goal")
                .targetAmount(new BigDecimal("1000.00"))
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2026, 1, 1))
                .user(user)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(goalRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(goal));
        when(transactionRepository.sumNetTransactionsBetweenDates(eq(user), eq(LocalDate.of(2025, 1, 1)), any(LocalDate.class)))
                .thenReturn(new BigDecimal("1500.00"));

        GoalResponse response = goalService.getGoal(1L, 1L);

        assertEquals(new BigDecimal("1500.00"), response.getCurrentProgress());
        assertEquals(BigDecimal.ZERO, response.getRemainingAmount()); // Snapped to zero
        assertTrue(response.getIsAchieved());
    }

    @Test
    public void testCreateGoalWithDefaultStartDate() {
        User user = new User();
        user.setId(1L);

        GoalRequest request = new GoalRequest();
        request.setGoalName("Test Goal");
        request.setTargetAmount(new BigDecimal("2000.00"));
        request.setTargetDate(LocalDate.of(2027, 1, 1));
        // startDate is null — should default to today

        Goal savedGoal = Goal.builder()
                .id(1L)
                .goalName("Test Goal")
                .targetAmount(new BigDecimal("2000.00"))
                .startDate(LocalDate.now())
                .endDate(LocalDate.of(2027, 1, 1))
                .user(user)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(goalRepository.save(any(Goal.class))).thenReturn(savedGoal);
        when(transactionRepository.sumNetTransactionsBetweenDates(eq(user), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(BigDecimal.ZERO);

        GoalResponse response = goalService.createGoal(1L, request);

        assertEquals("Test Goal", response.getGoalName());
        assertEquals(LocalDate.now(), response.getStartDate());
    }
}
