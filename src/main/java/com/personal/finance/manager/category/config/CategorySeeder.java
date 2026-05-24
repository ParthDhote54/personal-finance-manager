package com.personal.finance.manager.category.config;

import com.personal.finance.manager.category.entity.Category;
import com.personal.finance.manager.category.entity.CategoryType;
import com.personal.finance.manager.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Seeds default global categories into the database on startup.
 * Default categories are associated with user_id = null.
 */
@Component
@RequiredArgsConstructor
public class CategorySeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) throws Exception {
        seedCategories();
    }

    private void seedCategories() {
        // Requested lists
        List<String> incomeCategories = Arrays.asList("Salary");
        List<String> expenseCategories = Arrays.asList("Food", "Rent", "Transportation", "Entertainment", "Healthcare", "Utilities");

        for (String name : incomeCategories) {
            if (!categoryRepository.existsByNameAndUserIdIsNull(name)) {
                Category category = Category.builder()
                        .name(name)
                        .type(CategoryType.INCOME)
                        .user(null)
                        .build();
                categoryRepository.save(category);
            }
        }

        for (String name : expenseCategories) {
            if (!categoryRepository.existsByNameAndUserIdIsNull(name)) {
                Category category = Category.builder()
                        .name(name)
                        .type(CategoryType.EXPENSE)
                        .user(null)
                        .build();
                categoryRepository.save(category);
            }
        }
    }
}
