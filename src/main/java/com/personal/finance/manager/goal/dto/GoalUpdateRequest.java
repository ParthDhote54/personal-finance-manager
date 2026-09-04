package com.personal.finance.manager.goal.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalUpdateRequest {

    private String goalName;

    @Positive(message = "Target amount must be strictly positive")
    private BigDecimal targetAmount;

    private LocalDate startDate;

    @Future(message = "Target date must be in the future")
    private LocalDate targetDate;
}
