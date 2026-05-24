package com.personal.finance.manager.category.entity;

import com.personal.finance.manager.common.entity.BaseEntity;
import com.personal.finance.manager.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "categories", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"name", "user_id"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class Category extends BaseEntity {

    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoryType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
