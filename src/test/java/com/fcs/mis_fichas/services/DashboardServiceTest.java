package com.fcs.mis_fichas.services;

import com.fcs.mis_fichas.dtos.*;
import com.fcs.mis_fichas.entities.Category;
import com.fcs.mis_fichas.entities.Transaction;
import com.fcs.mis_fichas.entities.User;
import com.fcs.mis_fichas.enums.Role;
import com.fcs.mis_fichas.enums.Status;
import com.fcs.mis_fichas.enums.Type;
import com.fcs.mis_fichas.repositories.TransactionRepository;
import com.fcs.mis_fichas.repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private Authentication authentication;
    @Mock
    private SecurityContext securityContext;

    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardService(transactionRepository, userRepository);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private User mockAuthenticatedUser(String email, Role role) {
        User user = User.builder().id(1L).email(email).name("Test").role(role).status(Status.ACTIVE).build();
        when(authentication.getName()).thenReturn(email);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(userRepository.findByEmailAndDeletedAtIsNull(email)).thenReturn(Optional.of(user));
        return user;
    }

    @Test
    void getTotalIncome_shouldReturnSum() {
        mockAuthenticatedUser("user@example.com", Role.USER);
        when(transactionRepository.sumByUserAndTypeBetweenDates(eq(1L), eq(Type.INCOME), any(), any()))
                .thenReturn(new BigDecimal("500.00"));

        DashboardTotalResponse response = dashboardService.getTotalIncome(6, 2026);

        assertThat(response.total()).isEqualByComparingTo(new BigDecimal("500.00"));
    }

    @Test
    void getTotalIncome_shouldReturnZero_whenNoTransactions() {
        mockAuthenticatedUser("user@example.com", Role.USER);
        when(transactionRepository.sumByUserAndTypeBetweenDates(eq(1L), eq(Type.INCOME), any(), any()))
                .thenReturn(BigDecimal.ZERO);

        DashboardTotalResponse response = dashboardService.getTotalIncome(6, 2026);

        assertThat(response.total()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getTotalExpense_shouldReturnSum() {
        mockAuthenticatedUser("user@example.com", Role.USER);
        when(transactionRepository.sumByUserAndTypeBetweenDates(eq(1L), eq(Type.EXPENSE), any(), any()))
                .thenReturn(new BigDecimal("300.00"));

        DashboardTotalResponse response = dashboardService.getTotalExpense(6, 2026);

        assertThat(response.total()).isEqualByComparingTo(new BigDecimal("300.00"));
    }

    @Test
    void getExpensesByCategory_shouldReturnGrouped() {
        mockAuthenticatedUser("user@example.com", Role.USER);
        List<Object[]> raw = List.<Object[]>of(
                new Object[]{1L, "Food", new BigDecimal("200.00")},
                new Object[]{2L, "Transport", new BigDecimal("100.00")}
        );
        when(transactionRepository.expenseSumGroupedByCategory(eq(1L), any(), any())).thenReturn(raw);

        List<CategorySummaryResponse> result = dashboardService.getExpensesByCategory(6, 2026);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).categoryName()).isEqualTo("Food");
        assertThat(result.get(0).total()).isEqualByComparingTo(new BigDecimal("200.00"));
        assertThat(result.get(1).categoryName()).isEqualTo("Transport");
    }

    @Test
    void getExpensesByCategory_shouldReturnEmpty_whenNoData() {
        mockAuthenticatedUser("user@example.com", Role.USER);
        when(transactionRepository.expenseSumGroupedByCategory(eq(1L), any(), any())).thenReturn(List.of());

        List<CategorySummaryResponse> result = dashboardService.getExpensesByCategory(6, 2026);

        assertThat(result).isEmpty();
    }

    @Test
    void getExpensesBySubcategory_shouldReturnGrouped() {
        mockAuthenticatedUser("user@example.com", Role.USER);
        List<Object[]> raw = List.<Object[]>of(
                new Object[]{1L, "Groceries", 1L, "Food", new BigDecimal("150.00")},
                new Object[]{2L, "Eating out", 1L, "Food", new BigDecimal("50.00")}
        );
        when(transactionRepository.expenseSumGroupedBySubcategory(eq(1L), eq(1L), any(), any())).thenReturn(raw);

        List<SubcategorySummaryResponse> result = dashboardService.getExpensesBySubcategory(1L, 6, 2026);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).subcategoryName()).isEqualTo("Groceries");
        assertThat(result.get(0).total()).isEqualByComparingTo(new BigDecimal("150.00"));
    }

    @Test
    void getMonthlyBalance_shouldReturn12Months() {
        User user = mockAuthenticatedUser("user@example.com", Role.USER);
        Category incomeCat = Category.builder().id(1L).type(Type.INCOME).build();
        Category expenseCat = Category.builder().id(2L).type(Type.EXPENSE).build();
        YearMonth current = YearMonth.now();
        Transaction txn = Transaction.builder()
                .user(user)
                .category(incomeCat)
                .amount(new BigDecimal("1000.00"))
                .transactionDate(current.atDay(1))
                .build();

        when(transactionRepository.findTransactionsSince(eq(1L), any())).thenReturn(List.of(txn));

        List<MonthlyBalanceResponse> result = dashboardService.getMonthlyBalance();

        assertThat(result).hasSize(12);
        MonthlyBalanceResponse last = result.get(result.size() - 1);
        assertThat(last.year()).isEqualTo(current.getYear());
        assertThat(last.month()).isEqualTo(current.getMonthValue());
        assertThat(last.income()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(last.expense()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(last.balance()).isEqualByComparingTo(new BigDecimal("1000.00"));
    }

    @Test
    void getMonthlyBalance_shouldHandleMixedTypes() {
        User user = mockAuthenticatedUser("user@example.com", Role.USER);
        Category incomeCat = Category.builder().id(1L).type(Type.INCOME).build();
        Category expenseCat = Category.builder().id(2L).type(Type.EXPENSE).build();
        YearMonth current = YearMonth.now();
        List<Transaction> txns = List.of(
                Transaction.builder().user(user).category(incomeCat).amount(new BigDecimal("2000.00")).transactionDate(current.atDay(1)).build(),
                Transaction.builder().user(user).category(expenseCat).amount(new BigDecimal("500.00")).transactionDate(current.atDay(15)).build(),
                Transaction.builder().user(user).category(expenseCat).amount(new BigDecimal("300.00")).transactionDate(current.atDay(20)).build()
        );

        when(transactionRepository.findTransactionsSince(eq(1L), any())).thenReturn(txns);

        List<MonthlyBalanceResponse> result = dashboardService.getMonthlyBalance();

        MonthlyBalanceResponse currentMonth = result.get(result.size() - 1);
        assertThat(currentMonth.income()).isEqualByComparingTo(new BigDecimal("2000.00"));
        assertThat(currentMonth.expense()).isEqualByComparingTo(new BigDecimal("800.00"));
        assertThat(currentMonth.balance()).isEqualByComparingTo(new BigDecimal("1200.00"));
    }

    @Test
    void getAdminStats_shouldReturnCounts() {
        when(userRepository.countByDeletedAtIsNullAndRole(Role.USER)).thenReturn(10L);
        when(transactionRepository.count()).thenReturn(500L);

        AdminStatsResponse response = dashboardService.getAdminStats();

        assertThat(response.totalUsers()).isEqualTo(10L);
        assertThat(response.totalTransactions()).isEqualTo(500L);
    }

    @Test
    void getAdminExpensesByCategory_withUserIdZero_shouldPassNull() {
        when(transactionRepository.expenseSumGroupedByCategory(isNull(), any(), any())).thenReturn(List.of());

        List<CategorySummaryResponse> result = dashboardService.getAdminExpensesByCategory(0L, 1, 2026, 6, 2026);

        assertThat(result).isEmpty();
        verify(transactionRepository).expenseSumGroupedByCategory(isNull(), any(), any());
    }

    @Test
    void getAdminExpensesByCategory_withSpecificUserId_shouldFilter() {
        List<Object[]> raw = List.<Object[]>of(
                new Object[]{1L, "Food", new BigDecimal("300.00")}
        );
        when(transactionRepository.expenseSumGroupedByCategory(eq(5L), any(), any())).thenReturn(raw);

        List<CategorySummaryResponse> result = dashboardService.getAdminExpensesByCategory(5L, 1, 2026, 6, 2026);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).total()).isEqualByComparingTo(new BigDecimal("300.00"));
    }

    @Test
    void getAvgIncome_shouldReturn12MonthsWithAverages() {
        Category incomeCat = Category.builder().id(1L).type(Type.INCOME).build();
        YearMonth current = YearMonth.now();
        List<Transaction> txns = List.of(
                Transaction.builder().id(1L).category(incomeCat).amount(new BigDecimal("100.00")).transactionDate(current.atDay(1)).build(),
                Transaction.builder().id(2L).category(incomeCat).amount(new BigDecimal("200.00")).transactionDate(current.atDay(2)).build()
        );

        when(transactionRepository.findTransactionsSince(isNull(), any())).thenReturn(txns);

        List<MonthlyAverageResponse> result = dashboardService.getAvgIncome(0L);

        assertThat(result).hasSize(12);
        MonthlyAverageResponse currentMonth = result.get(result.size() - 1);
        assertThat(currentMonth.year()).isEqualTo(current.getYear());
        assertThat(currentMonth.month()).isEqualTo(current.getMonthValue());
        assertThat(currentMonth.average()).isEqualByComparingTo(new BigDecimal("150.00"));
    }

    @Test
    void getAvgIncome_shouldReturnZeroForEmptyMonths() {
        when(transactionRepository.findTransactionsSince(isNull(), any())).thenReturn(List.of());

        List<MonthlyAverageResponse> result = dashboardService.getAvgIncome(0L);

        assertThat(result).hasSize(12);
        assertThat(result.get(0).average()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getAvgIncome_withSpecificUserId_shouldFilter() {
        Category incomeCat = Category.builder().id(1L).type(Type.INCOME).build();
        YearMonth current = YearMonth.now();
        List<Transaction> txns = List.of(
                Transaction.builder().id(1L).category(incomeCat).amount(new BigDecimal("100.00")).transactionDate(current.atDay(1)).build()
        );

        when(transactionRepository.findTransactionsSince(eq(5L), any())).thenReturn(txns);

        List<MonthlyAverageResponse> result = dashboardService.getAvgIncome(5L);

        assertThat(result).hasSize(12);
        assertThat(result.get(result.size() - 1).average()).isEqualByComparingTo(new BigDecimal("100.00"));
        verify(transactionRepository).findTransactionsSince(eq(5L), any());
    }

    @Test
    void getAvgExpense_shouldReturn12MonthsWithAverages() {
        Category expenseCat = Category.builder().id(1L).type(Type.EXPENSE).build();
        YearMonth current = YearMonth.now();
        List<Transaction> txns = List.of(
                Transaction.builder().id(1L).category(expenseCat).amount(new BigDecimal("50.00")).transactionDate(current.atDay(1)).build(),
                Transaction.builder().id(2L).category(expenseCat).amount(new BigDecimal("150.00")).transactionDate(current.atDay(10)).build()
        );

        when(transactionRepository.findTransactionsSince(isNull(), any())).thenReturn(txns);

        List<MonthlyAverageResponse> result = dashboardService.getAvgExpense(0L);

        assertThat(result).hasSize(12);
        MonthlyAverageResponse currentMonth = result.get(result.size() - 1);
        assertThat(currentMonth.average()).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    void getAvgExpense_withSpecificUserId_shouldFilter() {
        Category expenseCat = Category.builder().id(1L).type(Type.EXPENSE).build();
        YearMonth current = YearMonth.now();
        List<Transaction> txns = List.of(
                Transaction.builder().id(1L).category(expenseCat).amount(new BigDecimal("75.00")).transactionDate(current.atDay(1)).build()
        );

        when(transactionRepository.findTransactionsSince(eq(3L), any())).thenReturn(txns);

        List<MonthlyAverageResponse> result = dashboardService.getAvgExpense(3L);

        assertThat(result).hasSize(12);
        assertThat(result.get(result.size() - 1).average()).isEqualByComparingTo(new BigDecimal("75.00"));
        verify(transactionRepository).findTransactionsSince(eq(3L), any());
    }
}
