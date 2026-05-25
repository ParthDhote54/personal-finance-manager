package com.personal.finance.manager.goal.entity;

import com.personal.finance.manager.common.entity.BaseEntity;
import com.personal.finance.manager.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "goals", indexes = {@Index(name = "idx_goal_user", columnList = "user_id")})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class Goal extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String goalName;

    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal targetAmount;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isAchieved = false;

    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal currentProgress = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
