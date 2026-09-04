package com.personal.finance.manager.category.service;

import com.personal.finance.manager.category.dto.CategoryRequest;
import com.personal.finance.manager.category.dto.CategoryResponse;
import com.personal.finance.manager.category.entity.Category;
import com.personal.finance.manager.category.entity.CategoryType;
import com.personal.finance.manager.category.repository.CategoryRepository;
import com.personal.finance.manager.exception.AccessDeniedException;
import com.personal.finance.manager.exception.CategoryInUseException;
import com.personal.finance.manager.exception.ResourceNotFoundException;
import com.personal.finance.manager.transaction.repository.TransactionRepository;
import com.personal.finance.manager.user.entity.User;
import com.personal.finance.manager.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private CategoryService categoryService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateCategorySuccess() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("testuser@example.com");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        User user = new User();
        user.setId(1L);
        user.setUsername("testuser@example.com");

        when(userRepository.findByUsername("testuser@example.com")).thenReturn(Optional.of(user));
        when(categoryRepository.existsByNameAndUserId("Subscriptions", 1L)).thenReturn(false);
        when(categoryRepository.existsByNameAndUserIdIsNull("Subscriptions")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(i -> {
            Category c = i.getArgument(0);
            c.setId(10L);
            return c;
        });

        CategoryRequest request = new CategoryRequest();
        request.setName("Subscriptions");
        request.setType(CategoryType.EXPENSE);

        CategoryResponse response = categoryService.createCategory(1L, request);

        assertNotNull(response);
        assertEquals("Subscriptions", response.getName());
        assertTrue(response.isCustom());
    }

    @Test
    public void testGetUserCategories() {
        Category defaultCat = Category.builder().id(1L).name("Salary").type(CategoryType.INCOME).user(null).build();
        User user = new User(); user.setId(2L);
        Category customCat = Category.builder().id(5L).name("Gym").type(CategoryType.EXPENSE).user(user).build();

        when(categoryRepository.findByUserIdIsNull()).thenReturn(new ArrayList<>(List.of(defaultCat)));
        when(categoryRepository.findByUserId(2L)).thenReturn(new ArrayList<>(List.of(customCat)));

        List<CategoryResponse> responses = categoryService.getUserCategories(2L);

        assertEquals(2, responses.size());
    }

    @Test
    public void testDeleteDefaultCategoryThrowsAccessDenied() {
        Category defaultCategory = new Category();
        defaultCategory.setId(1L);
        defaultCategory.setName("Salary");
        defaultCategory.setUser(null);

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

    @Test
    public void testDeleteCategoryByNameSuccess() {
        User user = new User();
        user.setId(1L);

        Category customCategory = Category.builder()
                .id(10L)
                .name("Hobbies")
                .user(user)
                .build();

        when(categoryRepository.existsByNameAndUserIdIsNull("Hobbies")).thenReturn(false);
        when(categoryRepository.findByNameAndUserId("Hobbies", 1L)).thenReturn(Optional.of(customCategory));
        when(transactionRepository.existsByCategoryId(10L)).thenReturn(false);

        categoryService.deleteCategoryByName(1L, "Hobbies");

        verify(categoryRepository, times(1)).delete(customCategory);
    }

    @Test
    public void testDeleteCategoryByNameDefaultCategoryThrowsAccessDenied() {
        when(categoryRepository.existsByNameAndUserIdIsNull("Salary")).thenReturn(true);

        assertThrows(AccessDeniedException.class, () -> categoryService.deleteCategoryByName(1L, "Salary"));
    }

    @Test
    public void testDeleteCategoryByNameInUseThrowsCategoryInUseException() {
        User user = new User();
        user.setId(1L);

        Category customCategory = Category.builder()
                .id(10L)
                .name("Hobbies")
                .user(user)
                .build();

        when(categoryRepository.existsByNameAndUserIdIsNull("Hobbies")).thenReturn(false);
        when(categoryRepository.findByNameAndUserId("Hobbies", 1L)).thenReturn(Optional.of(customCategory));
        when(transactionRepository.existsByCategoryId(10L)).thenReturn(true);

        assertThrows(CategoryInUseException.class, () -> categoryService.deleteCategoryByName(1L, "Hobbies"));
    }

    @Test
    public void testDeleteCategoryByNameNotFoundThrowsResourceNotFoundException() {
        when(categoryRepository.existsByNameAndUserIdIsNull("NonExistent")).thenReturn(false);
        when(categoryRepository.findByNameAndUserId("NonExistent", 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.deleteCategoryByName(1L, "NonExistent"));
    }
}
