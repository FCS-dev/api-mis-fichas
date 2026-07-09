package com.fcs.mis_fichas.controllers;

import com.fcs.mis_fichas.dtos.*;
import com.fcs.mis_fichas.services.DashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = DashboardController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = com.fcs.mis_fichas.config.JwtAuthenticationFilter.class))
@AutoConfigureMockMvc(addFilters = false)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DashboardService dashboardService;

    @Test
    void getTotalIncome_shouldReturn200() throws Exception {
        when(dashboardService.getTotalIncome(6, 2026)).thenReturn(new DashboardTotalResponse(new BigDecimal("500.00")));

        mockMvc.perform(get("/dashboard/me/total-income?month=6&year=2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.total").value(500.00));

        verify(dashboardService).getTotalIncome(6, 2026);
    }

    @Test
    void getTotalExpense_shouldReturn200() throws Exception {
        when(dashboardService.getTotalExpense(6, 2026)).thenReturn(new DashboardTotalResponse(new BigDecimal("300.00")));

        mockMvc.perform(get("/dashboard/me/total-expense?month=6&year=2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(300.00));

        verify(dashboardService).getTotalExpense(6, 2026);
    }

    @Test
    void getExpensesByCategory_shouldReturn200() throws Exception {
        List<CategorySummaryResponse> list = List.of(
                new CategorySummaryResponse(1L, "Food", new BigDecimal("200.00"))
        );
        when(dashboardService.getExpensesByCategory(6, 2026)).thenReturn(list);

        mockMvc.perform(get("/dashboard/me/expenses-by-category?month=6&year=2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].categoryId").value(1))
                .andExpect(jsonPath("$.data[0].categoryName").value("Food"))
                .andExpect(jsonPath("$.data[0].total").value(200.00));

        verify(dashboardService).getExpensesByCategory(6, 2026);
    }

    @Test
    void getExpensesBySubcategory_shouldReturn200() throws Exception {
        List<SubcategorySummaryResponse> list = List.of(
                new SubcategorySummaryResponse(1L, "Groceries", 1L, "Food", new BigDecimal("150.00"))
        );
        when(dashboardService.getExpensesBySubcategory(1L, 6, 2026)).thenReturn(list);

        mockMvc.perform(get("/dashboard/me/expenses-by-subcategory?categoryId=1&month=6&year=2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].subcategoryId").value(1))
                .andExpect(jsonPath("$.data[0].total").value(150.00));

        verify(dashboardService).getExpensesBySubcategory(1L, 6, 2026);
    }

    @Test
    void getMonthlyBalance_shouldReturn200() throws Exception {
        List<MonthlyBalanceResponse> list = List.of(
                new MonthlyBalanceResponse(2026, 6, new BigDecimal("2000.00"), new BigDecimal("800.00"), new BigDecimal("1200.00"))
        );
        when(dashboardService.getMonthlyBalance()).thenReturn(list);

        mockMvc.perform(get("/dashboard/me/monthly-balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].year").value(2026))
                .andExpect(jsonPath("$.data[0].month").value(6))
                .andExpect(jsonPath("$.data[0].income").value(2000.00))
                .andExpect(jsonPath("$.data[0].expense").value(800.00))
                .andExpect(jsonPath("$.data[0].balance").value(1200.00));

        verify(dashboardService).getMonthlyBalance();
    }

    @Test
    void getAdminStats_shouldReturn200() throws Exception {
        when(dashboardService.getAdminStats()).thenReturn(new AdminStatsResponse(10L, 500L));

        mockMvc.perform(get("/dashboard/admin/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalUsers").value(10))
                .andExpect(jsonPath("$.data.totalTransactions").value(500));

        verify(dashboardService).getAdminStats();
    }

    @Test
    void getAdminExpensesByCategory_shouldReturn200() throws Exception {
        List<CategorySummaryResponse> list = List.of(
                new CategorySummaryResponse(1L, "Food", new BigDecimal("1000.00"))
        );
        when(dashboardService.getAdminExpensesByCategory(0L, 1, 2026, 6, 2026)).thenReturn(list);

        mockMvc.perform(get("/dashboard/admin/expenses-by-category?monthFrom=1&yearFrom=2026&monthTo=6&yearTo=2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].total").value(1000.00));

        verify(dashboardService).getAdminExpensesByCategory(0L, 1, 2026, 6, 2026);
    }

    @Test
    void getAdminExpensesByCategory_withUserId_shouldReturn200() throws Exception {
        List<CategorySummaryResponse> list = List.of(
                new CategorySummaryResponse(1L, "Transport", new BigDecimal("300.00"))
        );
        when(dashboardService.getAdminExpensesByCategory(5L, 1, 2026, 6, 2026)).thenReturn(list);

        mockMvc.perform(get("/dashboard/admin/expenses-by-category?userId=5&monthFrom=1&yearFrom=2026&monthTo=6&yearTo=2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].categoryName").value("Transport"));

        verify(dashboardService).getAdminExpensesByCategory(5L, 1, 2026, 6, 2026);
    }

    @Test
    void getAdminExpensesBySubcategory_shouldReturn200() throws Exception {
        List<SubcategorySummaryResponse> list = List.of(
                new SubcategorySummaryResponse(1L, "Groceries", 1L, "Food", new BigDecimal("500.00"))
        );
        when(dashboardService.getAdminExpensesBySubcategory(0L, 1L, 1, 2026, 6, 2026)).thenReturn(list);

        mockMvc.perform(get("/dashboard/admin/expenses-by-subcategory?categoryId=1&monthFrom=1&yearFrom=2026&monthTo=6&yearTo=2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].total").value(500.00));

        verify(dashboardService).getAdminExpensesBySubcategory(0L, 1L, 1, 2026, 6, 2026);
    }

    @Test
    void getAvgIncome_shouldReturn200() throws Exception {
        List<MonthlyAverageResponse> list = List.of(
                new MonthlyAverageResponse(2026, 6, new BigDecimal("150.00"))
        );
        when(dashboardService.getAvgIncome(0L)).thenReturn(list);

        mockMvc.perform(get("/dashboard/admin/avg-income"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].average").value(150.00));

        verify(dashboardService).getAvgIncome(0L);
    }

    @Test
    void getAvgIncome_withUserId_shouldReturn200() throws Exception {
        List<MonthlyAverageResponse> list = List.of(
                new MonthlyAverageResponse(2026, 6, new BigDecimal("200.00"))
        );
        when(dashboardService.getAvgIncome(5L)).thenReturn(list);

        mockMvc.perform(get("/dashboard/admin/avg-income?userId=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].average").value(200.00));

        verify(dashboardService).getAvgIncome(5L);
    }

    @Test
    void getAvgExpense_shouldReturn200() throws Exception {
        List<MonthlyAverageResponse> list = List.of(
                new MonthlyAverageResponse(2026, 6, new BigDecimal("100.00"))
        );
        when(dashboardService.getAvgExpense(0L)).thenReturn(list);

        mockMvc.perform(get("/dashboard/admin/avg-expense"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].average").value(100.00));

        verify(dashboardService).getAvgExpense(0L);
    }

    @Test
    void getAvgExpense_withUserId_shouldReturn200() throws Exception {
        List<MonthlyAverageResponse> list = List.of(
                new MonthlyAverageResponse(2026, 6, new BigDecimal("80.00"))
        );
        when(dashboardService.getAvgExpense(3L)).thenReturn(list);

        mockMvc.perform(get("/dashboard/admin/avg-expense?userId=3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].average").value(80.00));

        verify(dashboardService).getAvgExpense(3L);
    }
}
