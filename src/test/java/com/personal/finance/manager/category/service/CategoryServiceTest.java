package com.personal.finance.manager.category.service;

import com.personal.finance.manager.category.entity.Category;
import com.personal.finance.manager.category.repository.CategoryRepository;
import com.personal.finance.manager.exception.AccessDeniedException;
import com.personal.finance.manager.exception.CategoryInUseException;
import com.personal.finance.manager.transaction.repository.TransactionRepository;
import com.personal.finance.manager.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private CategoryService categoryService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testDeleteDefaultCategoryThrowsAccessDenied() {
        Category defaultCategory = new Category();
        defaultCategory.setId(1L);
        defaultCategory.setName("Salary");
        defaultCategory.setUser(null); // No user = global default

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(defaultCategory));

        assertThrows(AccessDeniedException.class, () -> categoryService.deleteCategory(1L, 1L));
    }

    @Test
    public void testDeleteInUseCategoryThrowsConflict() {
        User user = new User();
        user.setId(1L);

        Category customCategory = new Category();
        customCategory.setId(2L);
        customCategory.setName("Custom");
        customCategory.setUser(user);

        when(categoryRepository.findById(2L)).thenReturn(Optional.of(customCategory));
        when(transactionRepository.existsByCategoryId(2L)).thenReturn(true);

        assertThrows(CategoryInUseException.class, () -> categoryService.deleteCategory(1L, 2L));
    }
}
