package com.personal.finance.manager.category.controller;

import com.personal.finance.manager.category.dto.CategoryListResponse;
import com.personal.finance.manager.category.dto.CategoryRequest;
import com.personal.finance.manager.category.dto.CategoryResponse;
import com.personal.finance.manager.category.service.CategoryService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Controller handling user category management.
 * Provides endpoints for retrieving global/custom categories, creating custom categories, and deleting custom categories.
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * Creates a new custom category for the authenticated user.
     *
     * @param request category creation payload
     * @param session authenticated HTTP session
     * @return created category details
     */
    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request, HttpSession session) {
        Long userId = (Long) session.getAttribute("USER_ID");
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        CategoryResponse response = categoryService.createCategory(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves all categories accessible to the authenticated user (global default + user custom).
     *
     * @param session authenticated HTTP session
     * @return wrapped list of accessible categories
     */
    @GetMapping
    public ResponseEntity<CategoryListResponse> getCategories(HttpSession session) {
        Long userId = (Long) session.getAttribute("USER_ID");
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        List<CategoryResponse> categories = categoryService.getUserCategories(userId);
        return ResponseEntity.ok(new CategoryListResponse(categories));
    }

    /**
     * Deletes a custom category by name for the authenticated user.
     *
     * @param name    category name to delete
     * @param session authenticated HTTP session
     * @return success message response map
     */
    @DeleteMapping("/{name}")
    public ResponseEntity<Map<String, String>> deleteCategory(@PathVariable String name, HttpSession session) {
        Long userId = (Long) session.getAttribute("USER_ID");
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        categoryService.deleteCategoryByName(userId, name);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Category deleted successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Updates an existing custom category by ID for the authenticated user.
     *
     * @param id      category ID
     * @param request category update payload
     * @param session authenticated HTTP session
     * @return updated category details
     */
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(@PathVariable Long id, 
                                                          @RequestBody CategoryRequest request, 
                                                          HttpSession session) {
        Long userId = (Long) session.getAttribute("USER_ID");
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        
        CategoryResponse updatedCategory = categoryService.updateCategory(id, userId, request);
        return ResponseEntity.ok(updatedCategory);
    }
}
