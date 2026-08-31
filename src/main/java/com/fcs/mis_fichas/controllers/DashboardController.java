package com.fcs.mis_fichas.controllers;

import com.fcs.mis_fichas.dtos.*;
import com.fcs.mis_fichas.services.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Dashboard con resúmenes, balances y estadísticas (USER y ADMIN)")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/me/total-income")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Total de ingresos del mes",
            description = "Devuelve la sumatoria de INCOME para el usuario autenticado en un mes y año dados.")
    public ResponseEntity<ApiResponse<DashboardTotalResponse>> getTotalIncome(
            @Parameter(description = "Mes (1-12)", example = "6") @RequestParam int month,
            @Parameter(description = "Año", example = "2026") @RequestParam int year,
            HttpServletRequest httpRequest) {
        DashboardTotalResponse data = dashboardService.getTotalIncome(month, year);
        return buildResponse(data, httpRequest);
    }

    @GetMapping("/me/total-expense")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Total de gastos del mes",
            description = "Devuelve la sumatoria de EXPENSE para el usuario autenticado en un mes y año dados.")
    public ResponseEntity<ApiResponse<DashboardTotalResponse>> getTotalExpense(
            @Parameter(description = "Mes (1-12)", example = "6") @RequestParam int month,
            @Parameter(description = "Año", example = "2026") @RequestParam int year,
            HttpServletRequest httpRequest) {
        DashboardTotalResponse data = dashboardService.getTotalExpense(month, year);
        return buildResponse(data, httpRequest);
    }

    @GetMapping("/me/expenses-by-category")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Gastos agrupados por categoría",
            description = "Devuelve la sumatoria de EXPENSE agrupada por categoría para el usuario autenticado en un mes y año dados.")
    public ResponseEntity<ApiResponse<List<CategorySummaryResponse>>> getExpensesByCategory(
            @Parameter(description = "Mes (1-12)", example = "6") @RequestParam int month,
            @Parameter(description = "Año", example = "2026") @RequestParam int year,
            HttpServletRequest httpRequest) {
        List<CategorySummaryResponse> data = dashboardService.getExpensesByCategory(month, year);
        return buildResponse(data, httpRequest);
    }

    @GetMapping("/me/expenses-by-subcategory")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Gastos agrupados por subcategoría",
            description = "Devuelve la sumatoria de EXPENSE agrupada por subcategoría dentro de una categoría específica, para el usuario autenticado en un mes y año dados.")
    public ResponseEntity<ApiResponse<List<SubcategorySummaryResponse>>> getExpensesBySubcategory(
            @Parameter(description = "ID de la categoría", example = "1") @RequestParam Long categoryId,
            @Parameter(description = "Mes (1-12)", example = "6") @RequestParam int month,
            @Parameter(description = "Año", example = "2026") @RequestParam int year,
            HttpServletRequest httpRequest) {
        List<SubcategorySummaryResponse> data = dashboardService.getExpensesBySubcategory(categoryId, month, year);
        return buildResponse(data, httpRequest);
    }

    @GetMapping("/me/monthly-balance")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Balance mensual de los últimos 12 meses",
            description = "Devuelve el balance (INCOME - EXPENSE) de cada uno de los últimos 12 meses para el usuario autenticado.")
    public ResponseEntity<ApiResponse<List<MonthlyBalanceResponse>>> getMonthlyBalance(HttpServletRequest httpRequest) {
        List<MonthlyBalanceResponse> data = dashboardService.getMonthlyBalance();
        return buildResponse(data, httpRequest);
    }

    @GetMapping("/admin/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Estadísticas generales",
            description = "Devuelve el total de usuarios activos y el total de transacciones registradas.")
    public ResponseEntity<ApiResponse<AdminStatsResponse>> getAdminStats(HttpServletRequest httpRequest) {
        AdminStatsResponse data = dashboardService.getAdminStats();
        return buildResponse(data, httpRequest);
    }

    @GetMapping("/admin/expenses-by-category")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Gastos agrupados por categoría (ADMIN)",
            description = "Devuelve la sumatoria de EXPENSE agrupada por categoría. " +
                    "userId opcional: si no se envía o es 0, incluye todos los usuarios; " +
                    "si es > 0, filtra por ese usuario.")
    public ResponseEntity<ApiResponse<List<CategorySummaryResponse>>> getAdminExpensesByCategory(
            @Parameter(description = "ID de usuario (0=todos)", example = "0") @RequestParam(required = false, defaultValue = "0") Long userId,
            @Parameter(description = "Mes inicio (1-12)", example = "1") @RequestParam int monthFrom,
            @Parameter(description = "Año inicio", example = "2026") @RequestParam int yearFrom,
            @Parameter(description = "Mes fin (1-12)", example = "6") @RequestParam int monthTo,
            @Parameter(description = "Año fin", example = "2026") @RequestParam int yearTo,
            HttpServletRequest httpRequest) {
        List<CategorySummaryResponse> data = dashboardService.getAdminExpensesByCategory(userId, monthFrom, yearFrom, monthTo, yearTo);
        return buildResponse(data, httpRequest);
    }

    @GetMapping("/admin/expenses-by-subcategory")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Gastos agrupados por subcategoría (ADMIN)",
            description = "Devuelve la sumatoria de EXPENSE agrupada por subcategoría dentro de una categoría. " +
                    "userId opcional: si no se envía o es 0, incluye todos los usuarios; " +
                    "si es > 0, filtra por ese usuario.")
    public ResponseEntity<ApiResponse<List<SubcategorySummaryResponse>>> getAdminExpensesBySubcategory(
            @Parameter(description = "ID de usuario (0=todos)", example = "0") @RequestParam(required = false, defaultValue = "0") Long userId,
            @Parameter(description = "ID de la categoría", example = "1") @RequestParam Long categoryId,
            @Parameter(description = "Mes inicio (1-12)", example = "1") @RequestParam int monthFrom,
            @Parameter(description = "Año inicio", example = "2026") @RequestParam int yearFrom,
            @Parameter(description = "Mes fin (1-12)", example = "6") @RequestParam int monthTo,
            @Parameter(description = "Año fin", example = "2026") @RequestParam int yearTo,
            HttpServletRequest httpRequest) {
        List<SubcategorySummaryResponse> data = dashboardService.getAdminExpensesBySubcategory(userId, categoryId, monthFrom, yearFrom, monthTo, yearTo);
        return buildResponse(data, httpRequest);
    }

    @GetMapping("/admin/avg-income")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Promedio mensual de ingresos (últimos 12 meses)",
            description = "Devuelve el monto promedio de INCOME de cada uno de los últimos 12 meses. " +
                    "userId opcional: si no se envía o es 0, incluye todos los usuarios; " +
                    "si es > 0, filtra por ese usuario.")
    public ResponseEntity<ApiResponse<List<MonthlyAverageResponse>>> getAvgIncome(
            @Parameter(description = "ID de usuario (0=todos)", example = "0") @RequestParam(required = false, defaultValue = "0") Long userId,
            HttpServletRequest httpRequest) {
        List<MonthlyAverageResponse> data = dashboardService.getAvgIncome(userId);
        return buildResponse(data, httpRequest);
    }

    @GetMapping("/admin/avg-expense")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Promedio mensual de gastos (últimos 12 meses)",
            description = "Devuelve el monto promedio de EXPENSE de cada uno de los últimos 12 meses. " +
                    "userId opcional: si no se envía o es 0, incluye todos los usuarios; " +
                    "si es > 0, filtra por ese usuario.")
    public ResponseEntity<ApiResponse<List<MonthlyAverageResponse>>> getAvgExpense(
            @Parameter(description = "ID de usuario (0=todos)", example = "0") @RequestParam(required = false, defaultValue = "0") Long userId,
            HttpServletRequest httpRequest) {
        List<MonthlyAverageResponse> data = dashboardService.getAvgExpense(userId);
        return buildResponse(data, httpRequest);
    }

    @GetMapping("/admin/user-evolution")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Evolución de usuarios",
            description = "Devuelve el resumen comparativo y datos mensuales de usuarios activos, registrados y nuevos en un rango de meses.")
    public ResponseEntity<ApiResponse<UserEvolutionResponse>> getUserEvolution(
            @Parameter(description = "Mes inicio (1-12)", example = "1") @RequestParam int monthFrom,
            @Parameter(description = "Año inicio", example = "2026") @RequestParam int yearFrom,
            @Parameter(description = "Mes fin (1-12)", example = "6") @RequestParam int monthTo,
            @Parameter(description = "Año fin", example = "2026") @RequestParam int yearTo,
            HttpServletRequest httpRequest) {
        UserEvolutionResponse data = dashboardService.getUserEvolution(monthFrom, yearFrom, monthTo, yearTo);
        return buildResponse(data, httpRequest);
    }

    @GetMapping("/admin/transaction-evolution")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Evolución de transacciones",
            description = "Devuelve el resumen comparativo y datos mensuales de transacciones, promedio por usuario, ingresos y gastos en un rango de meses.")
    public ResponseEntity<ApiResponse<TransactionEvolutionResponse>> getTransactionEvolution(
            @Parameter(description = "Mes inicio (1-12)", example = "1") @RequestParam int monthFrom,
            @Parameter(description = "Año inicio", example = "2026") @RequestParam int yearFrom,
            @Parameter(description = "Mes fin (1-12)", example = "6") @RequestParam int monthTo,
            @Parameter(description = "Año fin", example = "2026") @RequestParam int yearTo,
            @Parameter(description = "ID del usuario (0 = todos)") @RequestParam(defaultValue = "0") Long userId,
            HttpServletRequest httpRequest) {
        TransactionEvolutionResponse data = dashboardService.getTransactionEvolution(monthFrom, yearFrom, monthTo, yearTo, userId);
        return buildResponse(data, httpRequest);
    }

    @GetMapping("/admin/money-movement")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Movimientos de dinero",
            description = "Devuelve el total de ingresos, gastos y balance. " +
                    "userId opcional: si no se envía o es 0, incluye todos los usuarios; " +
                    "si es > 0, filtra por ese usuario.")
    public ResponseEntity<ApiResponse<MoneyMovementResponse>> getMoneyMovement(
            @Parameter(description = "ID de usuario (0=todos)", example = "0") @RequestParam(required = false, defaultValue = "0") Long userId,
            HttpServletRequest httpRequest) {
        MoneyMovementResponse data = dashboardService.getMoneyMovement(userId);
        return buildResponse(data, httpRequest);
    }

    @GetMapping("/admin/averages")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Promedios de ingresos, gastos y transacciones",
            description = "Devuelve promedios globales (todos los usuarios) y promedios filtrados por usuario. " +
                    "userId opcional: si no se envía o es 0, solo retorna globales; " +
                    "si es > 0, agrega los promedios filtrados de ese usuario.")
    public ResponseEntity<ApiResponse<DashboardAveragesResponse>> getDashboardAverages(
            @Parameter(description = "ID de usuario (0=todos)", example = "0") @RequestParam(required = false, defaultValue = "0") Long userId,
            HttpServletRequest httpRequest) {
        DashboardAveragesResponse data = dashboardService.getDashboardAverages(userId);
        return buildResponse(data, httpRequest);
    }

    @GetMapping("/admin/top-users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Top 5 usuarios por actividad",
            description = "Devuelve el top 5 de usuarios con más transacciones, más gastos y más ingresos en un rango de meses.")
    public ResponseEntity<ApiResponse<TopUsersResponse>> getTopUsers(
            @Parameter(description = "Mes inicio (1-12)", example = "1") @RequestParam int monthFrom,
            @Parameter(description = "Año inicio", example = "2026") @RequestParam int yearFrom,
            @Parameter(description = "Mes fin (1-12)", example = "6") @RequestParam int monthTo,
            @Parameter(description = "Año fin", example = "2026") @RequestParam int yearTo,
            HttpServletRequest httpRequest) {
        TopUsersResponse data = dashboardService.getTopUsers(monthFrom, yearFrom, monthTo, yearTo);
        return buildResponse(data, httpRequest);
    }

    @GetMapping("/admin/activity-distribution")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Distribución de usuarios por actividad",
            description = "Devuelve la distribución de usuarios por nivel de actividad en un mes: Frecuente (>20 tx), Regular (5-20), Ocasional (1-4), Inactivo (0).")
    public ResponseEntity<ApiResponse<ActivityDistributionResponse>> getActivityDistribution(
            @Parameter(description = "Mes (1-12)", example = "6") @RequestParam int month,
            @Parameter(description = "Año", example = "2026") @RequestParam int year,
            HttpServletRequest httpRequest) {
        ActivityDistributionResponse data = dashboardService.getActivityDistribution(month, year);
        return buildResponse(data, httpRequest);
    }

    private <T> ResponseEntity<ApiResponse<T>> buildResponse(T data, HttpServletRequest httpRequest) {
        ApiResponse<T> apiResponse = new ApiResponse<>(
                true, HttpStatus.OK.value(), null, data,
                LocalDateTime.now(), httpRequest.getRequestURI()
        );
        return ResponseEntity.ok(apiResponse);
    }
}
