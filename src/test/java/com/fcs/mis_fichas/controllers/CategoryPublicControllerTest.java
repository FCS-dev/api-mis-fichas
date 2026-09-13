package com.fcs.mis_fichas.controllers;

import com.fcs.mis_fichas.config.RateLimitInterceptor;
import com.fcs.mis_fichas.dtos.CategoryResponse;
import com.fcs.mis_fichas.enums.Type;
import com.fcs.mis_fichas.services.CategoryService;
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
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CategoryPublicController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = com.fcs.mis_fichas.config.JwtAuthenticationFilter.class))
@AutoConfigureMockMvc(addFilters = false)
class CategoryPublicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private RateLimitInterceptor rateLimitInterceptor;

    private static CategoryResponse sampleResponse(Long id, String name) {
        return new CategoryResponse(id, name, Type.INCOME, 1L, "user@example.com",
                LocalDateTime.now(), LocalDateTime.now(), null);
    }

    @Test
    void findAll_shouldReturn200_withPaginatedCategories() throws Exception {
        CategoryResponse response = sampleResponse(1L, "Salary");
        Page<CategoryResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 20, Sort.by("name").ascending()), 1);

        when(categoryService.findAll(any())).thenReturn(page);

        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].name").value("Salary"))
                .andExpect(jsonPath("$.data.pagination.totalElements").value(1));
    }

    @Test
    void findAll_shouldAcceptCustomPaginationParams() throws Exception {
        CategoryResponse response = sampleResponse(1L, "Salary");
        Page<CategoryResponse> page = new PageImpl<>(List.of(response), PageRequest.of(1, 5, Sort.by("name").descending()), 1);

        when(categoryService.findAll(any())).thenReturn(page);

        mockMvc.perform(get("/categories?page=1&size=5&sort=name,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pagination.currentPage").value(1))
                .andExpect(jsonPath("$.data.pagination.pageSize").value(5))
                .andExpect(jsonPath("$.data.pagination.sort").value("name: DESC"));
    }
}
