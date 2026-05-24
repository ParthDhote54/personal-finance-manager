package com.personal.finance.manager.goal.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class GoalRequest {
    @NotBlank(message = "Goal name is required")
    private String goalName;

    @NotNull(message = "Target amount is required")
    @Positive(message = "Target amount must be strictly positive")
    private BigDecimal targetAmount;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "Target date is required")
    private LocalDate targetDate;

    @AssertTrue(message = "Target date must be after start date")
    public boolean isValidDateRange() {
        if (startDate == null || targetDate == null) {
            return true;
        }
        return targetDate.isAfter(startDate);
    }
}
