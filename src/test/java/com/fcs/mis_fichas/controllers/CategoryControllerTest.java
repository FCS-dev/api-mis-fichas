package com.fcs.mis_fichas.controllers;

import com.fcs.mis_fichas.config.RateLimitInterceptor;
import com.fcs.mis_fichas.dtos.CategoryRequest;
import com.fcs.mis_fichas.dtos.CategoryResponse;
import com.fcs.mis_fichas.dtos.PagedResponse;
import com.fcs.mis_fichas.dtos.PaginationInfo;
import com.fcs.mis_fichas.enums.Type;
import com.fcs.mis_fichas.services.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CategoryController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = com.fcs.mis_fichas.config.JwtAuthenticationFilter.class))
@AutoConfigureMockMvc(addFilters = false)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private RateLimitInterceptor rateLimitInterceptor;

    private static CategoryResponse sampleResponse(Long id, String name) {
        return new CategoryResponse(id, name, Type.EXPENSE, 1L, "user@example.com",
                LocalDateTime.now(), LocalDateTime.now(), null);
    }

    @Test
    void create_shouldReturn201_whenRequestValid() throws Exception {
        CategoryRequest request = new CategoryRequest("Food", Type.EXPENSE);
        CategoryResponse response = sampleResponse(1L, "Food");

        when(categoryService.create(any(CategoryRequest.class))).thenReturn(response);

        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Food"));

        verify(categoryService).create(any(CategoryRequest.class));
    }

    @Test
    void update_shouldReturn200_whenRequestValid() throws Exception {
        CategoryRequest request = new CategoryRequest("Groceries", Type.EXPENSE);
        CategoryResponse response = sampleResponse(1L, "Groceries");

        when(categoryService.update(eq(1L), any(CategoryRequest.class))).thenReturn(response);

        mockMvc.perform(put("/admin/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Groceries"));

        verify(categoryService).update(eq(1L), any(CategoryRequest.class));
    }

    @Test
    void delete_shouldReturn200_whenIdExists() throws Exception {
        mockMvc.perform(delete("/admin/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Category deleted successfully"));

        verify(categoryService).delete(1L);
    }

    @Test
    void findById_shouldReturn200_whenCategoryExists() throws Exception {
        CategoryResponse response = sampleResponse(1L, "Food");

        when(categoryService.findById(1L)).thenReturn(response);

        mockMvc.perform(get("/admin/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Food"));
    }

    @Test
    void findAll_shouldReturn200_withPaginationDefaults() throws Exception {
        CategoryResponse response = sampleResponse(1L, "Food");
        Page<CategoryResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 20, Sort.by("name").ascending()), 1);

        when(categoryService.findAll(any())).thenReturn(page);

        mockMvc.perform(get("/admin/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].name").value("Food"))
                .andExpect(jsonPath("$.data.pagination.currentPage").value(0))
                .andExpect(jsonPath("$.data.pagination.pageSize").value(20));
    }

    @Test
    void findAll_shouldRespectCustomPaginationParams() throws Exception {
        CategoryResponse response = sampleResponse(1L, "Food");
        Page<CategoryResponse> page = new PageImpl<>(List.of(response), PageRequest.of(2, 10, Sort.by("name").descending()), 1);

        when(categoryService.findAll(any())).thenReturn(page);

        mockMvc.perform(get("/admin/categories?page=2&size=10&sort=name,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pagination.currentPage").value(2))
                .andExpect(jsonPath("$.data.pagination.pageSize").value(10))
                .andExpect(jsonPath("$.data.pagination.sort").value("name: DESC"));
    }

    @Test
    void findAll_shouldCapSizeAtMax100() throws Exception {
        CategoryResponse response = sampleResponse(1L, "Food");
        Page<CategoryResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 100, Sort.by("name").ascending()), 1);

        when(categoryService.findAll(any())).thenReturn(page);

        mockMvc.perform(get("/admin/categories?size=500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pagination.pageSize").value(100));
    }

    @Test
    void findAll_shouldNormalizeNegativePageToZero() throws Exception {
        CategoryResponse response = sampleResponse(1L, "Food");
        Page<CategoryResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 20, Sort.by("name").ascending()), 1);

        when(categoryService.findAll(any())).thenReturn(page);

        mockMvc.perform(get("/admin/categories?page=-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pagination.currentPage").value(0));
    }
}
