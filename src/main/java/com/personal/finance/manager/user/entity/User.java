package com.personal.finance.manager.user.entity;

import com.personal.finance.manager.category.entity.Category;
import com.personal.finance.manager.common.entity.BaseEntity;
import com.personal.finance.manager.goal.entity.Goal;
import com.personal.finance.manager.transaction.entity.Transaction;
import jakarta.persistence.*;
import lombok.*;

import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class User extends BaseEntity {

    @Column(unique = true, nullable = false, length = 100)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(length = 20)
    private String phoneNumber;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(name = "profile_image", columnDefinition = "TEXT")
    private String profileImage;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Transaction> transactions = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Category> categories = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Goal> goals = new ArrayList<>();
}
