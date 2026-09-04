package com.personal.finance.manager.transaction.dto;

import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
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
public class TransactionUpdateRequest {

    @Positive(message = "Amount must be strictly positive")
    private BigDecimal amount;

    @PastOrPresent(message = "Transaction date cannot be in the future")
    private LocalDate date;

    private String category;

    @Size(max = 255, message = "Description too long")
    private String description;
}
