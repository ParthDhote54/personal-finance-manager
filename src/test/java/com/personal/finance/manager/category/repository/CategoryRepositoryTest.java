package com.personal.finance.manager.category.repository;

import com.personal.finance.manager.category.entity.Category;
import com.personal.finance.manager.category.entity.CategoryType;
import com.personal.finance.manager.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CategoryRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void findByIdAndUserIdOrUserIdNull_shouldReturnCorrectCategories() {
        // Setup
        User user = User.builder()
                .username("testuser")
                .password("password")
                .fullName("Test User")
                .build();
        entityManager.persist(user);

        Category globalCategory = Category.builder()
                .name("Global Income")
                .type(CategoryType.INCOME)
                .user(null)
                .build();
        entityManager.persist(globalCategory);

        Category userCategory = Category.builder()
                .name("User Income")
                .type(CategoryType.INCOME)
                .user(user)
                .build();
        entityManager.persist(userCategory);

        entityManager.flush();

        // Assert: Querying with user's ID returns global category
        Optional<Category> foundGlobal = categoryRepository.findByIdAndUserIdOrUserIdNull(globalCategory.getId(), user.getId());
        assertThat(foundGlobal).isPresent();
        assertThat(foundGlobal.get().getName()).isEqualTo("Global Income");

        // Assert: Querying with user's ID returns user category
        Optional<Category> foundUser = categoryRepository.findByIdAndUserIdOrUserIdNull(userCategory.getId(), user.getId());
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo("User Income");

        // Assert: Querying with non-existent user ID returns ONLY global category
        Optional<Category> foundGlobalAnon = categoryRepository.findByIdAndUserIdOrUserIdNull(globalCategory.getId(), 999L);
        assertThat(foundGlobalAnon).isPresent();

        Optional<Category> foundUserAnon = categoryRepository.findByIdAndUserIdOrUserIdNull(userCategory.getId(), 999L);
        assertThat(foundUserAnon).isEmpty();
    }
}
