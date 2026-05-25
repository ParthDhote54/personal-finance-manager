package com.personal.finance.manager.category.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.finance.manager.category.dto.CategoryRequest;
import com.personal.finance.manager.category.dto.CategoryResponse;
import com.personal.finance.manager.category.entity.CategoryType;
import com.personal.finance.manager.category.service.CategoryService;
import com.personal.finance.manager.exception.AccessDeniedException;
import com.personal.finance.manager.exception.CategoryInUseException;
import com.personal.finance.manager.exception.DuplicateResourceException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import com.personal.finance.manager.exception.AccessDeniedException;
import com.personal.finance.manager.exception.CategoryInUseException;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
public class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoryService categoryService;

    @Test
    public void testGetCategoriesWithSessionSuccess() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("USER_ID", 1L);

        when(categoryService.getUserCategories(1L)).thenReturn(List.of(
                new CategoryResponse(1L, "Salary", CategoryType.INCOME, false),
                new CategoryResponse(1L, "Custom", CategoryType.EXPENSE, true)
        ));

        mockMvc.perform(get("/api/categories").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Salary"))
                .andExpect(jsonPath("$[1].type").value("EXPENSE"));
    }

    @Test
    public void testGetCategoriesWithoutSessionUnauthorized() throws Exception {
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testCreateCategorySuccess() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("USER_ID", 1L);

        CategoryRequest request = new CategoryRequest();
        request.setName("SideBusiness");
        request.setType(CategoryType.INCOME);

        when(categoryService.createCategory(eq(1L), any(CategoryRequest.class)))
                .thenReturn(new CategoryResponse(1L, "SideBusiness", CategoryType.INCOME, true));

        mockMvc.perform(post("/api/categories")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("SideBusiness"))
                .andExpect(jsonPath("$.type").value("INCOME"));
    }

    @Test
    public void testDuplicateCategoryCreation() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("USER_ID", 1L);

        CategoryRequest request = new CategoryRequest();
        request.setName("Salary"); // Trying to duplicate default
        request.setType(CategoryType.INCOME);

        when(categoryService.createCategory(eq(1L), any(CategoryRequest.class)))
                .thenThrow(new DuplicateResourceException("Category name already exists"));

        mockMvc.perform(post("/api/categories")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    public void testDeleteCategorySuccess() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("USER_ID", 1L);

        mockMvc.perform(delete("/api/categories/2").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Category deleted successfully"));
    }

    @Test
    public void testDeleteGlobalCategoryForbidden() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("USER_ID", 1L);

        doThrow(new AccessDeniedException("Cannot delete default categories"))
                .when(categoryService).deleteCategory(1L, 1L);

        mockMvc.perform(delete("/api/categories/1").session(session))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testDeleteInUseCategoryConflict() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("USER_ID", 1L);

        doThrow(new CategoryInUseException("Cannot delete category in use by transactions"))
                .when(categoryService).deleteCategory(1L, 2L);

        mockMvc.perform(delete("/api/categories/2").session(session))
                .andExpect(status().isConflict());
    }
}
