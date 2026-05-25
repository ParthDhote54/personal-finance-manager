package com.personal.finance.manager.category.service;

import com.personal.finance.manager.category.dto.CategoryRequest;
import com.personal.finance.manager.category.dto.CategoryResponse;
import com.personal.finance.manager.category.entity.Category;
import com.personal.finance.manager.user.entity.User;
import com.personal.finance.manager.exception.AccessDeniedException;
import com.personal.finance.manager.exception.CategoryInUseException;
import com.personal.finance.manager.exception.DuplicateResourceException;
import com.personal.finance.manager.exception.ResourceNotFoundException;
import com.personal.finance.manager.category.repository.CategoryRepository;
import com.personal.finance.manager.transaction.repository.TransactionRepository;
import com.personal.finance.manager.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public CategoryResponse createCategory(Long userId, CategoryRequest request) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (categoryRepository.existsByNameAndUserId(request.getName(), user.getId()) ||
            categoryRepository.existsByNameAndUserIdIsNull(request.getName())) {
            throw new DuplicateResourceException("Category name already exists");
        }

        Category category = Category.builder()
                .name(request.getName())
                .type(request.getType())
                .user(user)
                .build();

        category = categoryRepository.save(category);
        return mapToResponse(category);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getUserCategories(Long userId) {
        List<Category> defaults = categoryRepository.findByUserIdIsNull();
        List<Category> custom = categoryRepository.findAll().stream()
                .filter(c -> c.getUser() != null && c.getUser().getId().equals(userId))
                .collect(Collectors.toList());

        defaults.addAll(custom);
        return defaults.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public void deleteCategory(Long userId, Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (category.getUser() == null) {
            throw new AccessDeniedException("Cannot delete default categories");
        }

        if (!category.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Cannot delete another user's category");
        }

        if (transactionRepository.existsByCategoryId(category.getId())) {
            throw new CategoryInUseException("Cannot delete category in use by transactions");
        }

        categoryRepository.delete(category);
    }

    @Transactional
    public CategoryResponse updateCategory(Long categoryId, Long userId, CategoryRequest request) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (category.getUser() == null || !category.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Cannot update another user's category or default category");
        }

        if (!category.getName().equals(request.getName())) {
            if (categoryRepository.existsByNameAndUserId(request.getName(), userId) ||
                categoryRepository.existsByNameAndUserIdIsNull(request.getName())) {
                throw new DuplicateResourceException("Category name already exists");
            }
        }

        category.setName(request.getName());
        category.setType(request.getType());
        
        return mapToResponse(categoryRepository.save(category));
    }

    private CategoryResponse mapToResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .type(category.getType())
                .isCustom(category.getUser() != null)
                .build();
    }
}
