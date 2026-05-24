package com.personal.finance.manager.transaction.entity;

import com.personal.finance.manager.category.entity.Category;
import com.personal.finance.manager.common.entity.BaseEntity;
import com.personal.finance.manager.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_user_date", columnList = "user_id, date")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class Transaction extends BaseEntity {

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(length = 255)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
