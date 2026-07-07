package com.fcs.mis_fichas.services;

import com.fcs.mis_fichas.dtos.*;
import com.fcs.mis_fichas.entities.Transaction;
import com.fcs.mis_fichas.entities.User;
import com.fcs.mis_fichas.enums.Role;
import com.fcs.mis_fichas.enums.Type;
import com.fcs.mis_fichas.repositories.TransactionRepository;
import com.fcs.mis_fichas.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    private static final int MONTHS_HISTORY = 12;

    public DashboardTotalResponse getTotalIncome(int month, int year) {
        User user = getCurrentUser();
        LocalDate start = YearMonth.of(year, month).atDay(1);
        LocalDate end = YearMonth.of(year, month).atEndOfMonth();
        BigDecimal total = transactionRepository.sumByUserAndTypeBetweenDates(user.getId(), Type.INCOME, start, end);
        return new DashboardTotalResponse(total);
    }

    public DashboardTotalResponse getTotalExpense(int month, int year) {
        User user = getCurrentUser();
        LocalDate start = YearMonth.of(year, month).atDay(1);
        LocalDate end = YearMonth.of(year, month).atEndOfMonth();
        BigDecimal total = transactionRepository.sumByUserAndTypeBetweenDates(user.getId(), Type.EXPENSE, start, end);
        return new DashboardTotalResponse(total);
    }

    public List<CategorySummaryResponse> getExpensesByCategory(int month, int year) {
        User user = getCurrentUser();
        LocalDate start = YearMonth.of(year, month).atDay(1);
        LocalDate end = YearMonth.of(year, month).atEndOfMonth();
        return mapToCategorySummaries(transactionRepository.expenseSumGroupedByCategory(user.getId(), start, end));
    }

    public List<SubcategorySummaryResponse> getExpensesBySubcategory(Long categoryId, int month, int year) {
        User user = getCurrentUser();
        LocalDate start = YearMonth.of(year, month).atDay(1);
        LocalDate end = YearMonth.of(year, month).atEndOfMonth();
        return mapToSubcategorySummaries(transactionRepository.expenseSumGroupedBySubcategory(user.getId(), categoryId, start, end));
    }

    public List<MonthlyBalanceResponse> getMonthlyBalance() {
        User user = getCurrentUser();
        LocalDate since = YearMonth.now().minusMonths(MONTHS_HISTORY - 1).atDay(1);
        List<Transaction> transactions = transactionRepository.findTransactionsSince(user.getId(), since);
        return computeMonthlyBalances(transactions);
    }

    public AdminStatsResponse getAdminStats() {
        long totalUsers = userRepository.countByDeletedAtIsNull();
        long totalTransactions = transactionRepository.count();
        return new AdminStatsResponse(totalUsers, totalTransactions);
    }

    public List<CategorySummaryResponse> getAdminExpensesByCategory(Long userId, int monthFrom, int yearFrom, int monthTo, int yearTo) {
        Long effectiveUserId = (userId != null && userId == 0L) ? null : userId;
        LocalDate start = YearMonth.of(yearFrom, monthFrom).atDay(1);
        LocalDate end = YearMonth.of(yearTo, monthTo).atEndOfMonth();
        return mapToCategorySummaries(transactionRepository.expenseSumGroupedByCategory(effectiveUserId, start, end));
    }

    public List<SubcategorySummaryResponse> getAdminExpensesBySubcategory(Long userId, Long categoryId, int monthFrom, int yearFrom, int monthTo, int yearTo) {
        Long effectiveUserId = (userId != null && userId == 0L) ? null : userId;
        LocalDate start = YearMonth.of(yearFrom, monthFrom).atDay(1);
        LocalDate end = YearMonth.of(yearTo, monthTo).atEndOfMonth();
        return mapToSubcategorySummaries(transactionRepository.expenseSumGroupedBySubcategory(effectiveUserId, categoryId, start, end));
    }

    public List<MonthlyAverageResponse> getAvgIncome() {
        return computeMonthlyAverages(Type.INCOME);
    }

    public List<MonthlyAverageResponse> getAvgExpense() {
        return computeMonthlyAverages(Type.EXPENSE);
    }

    private List<MonthlyAverageResponse> computeMonthlyAverages(Type type) {
        LocalDate since = YearMonth.now().minusMonths(MONTHS_HISTORY - 1).atDay(1);
        List<Transaction> transactions = transactionRepository.findTransactionsSince(null, since);
        Map<YearMonth, List<BigDecimal>> grouped = transactions.stream()
                .filter(t -> t.getCategory().getType() == type)
                .collect(Collectors.groupingBy(
                        t -> YearMonth.from(t.getTransactionDate()),
                        Collectors.mapping(Transaction::getAmount, Collectors.toList())
                ));
        List<MonthlyAverageResponse> result = new ArrayList<>();
        YearMonth start = YearMonth.now().minusMonths(MONTHS_HISTORY - 1);
        for (int i = 0; i < MONTHS_HISTORY; i++) {
            YearMonth ym = start.plusMonths(i);
            List<BigDecimal> amounts = grouped.getOrDefault(ym, Collections.emptyList());
            BigDecimal avg = amounts.isEmpty()
                    ? BigDecimal.ZERO
                    : amounts.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(amounts.size()), 2, java.math.RoundingMode.HALF_UP);
            result.add(new MonthlyAverageResponse(ym.getYear(), ym.getMonthValue(), avg));
        }
        return result;
    }

    private List<MonthlyBalanceResponse> computeMonthlyBalances(List<Transaction> transactions) {
        Map<YearMonth, List<Transaction>> grouped = transactions.stream()
                .collect(Collectors.groupingBy(t -> YearMonth.from(t.getTransactionDate())));
        List<MonthlyBalanceResponse> result = new ArrayList<>();
        YearMonth start = YearMonth.now().minusMonths(MONTHS_HISTORY - 1);
        for (int i = 0; i < MONTHS_HISTORY; i++) {
            YearMonth ym = start.plusMonths(i);
            List<Transaction> monthTxns = grouped.getOrDefault(ym, Collections.emptyList());
            BigDecimal income = monthTxns.stream()
                    .filter(t -> t.getCategory().getType() == Type.INCOME)
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal expense = monthTxns.stream()
                    .filter(t -> t.getCategory().getType() == Type.EXPENSE)
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal balance = income.subtract(expense);
            result.add(new MonthlyBalanceResponse(ym.getYear(), ym.getMonthValue(), income, expense, balance));
        }
        return result;
    }

    private List<CategorySummaryResponse> mapToCategorySummaries(List<Object[]> raw) {
        return raw.stream()
                .map(row -> new CategorySummaryResponse(
                        (Long) row[0],
                        (String) row[1],
                        (BigDecimal) row[2]
                ))
                .toList();
    }

    private List<SubcategorySummaryResponse> mapToSubcategorySummaries(List<Object[]> raw) {
        return raw.stream()
                .map(row -> new SubcategorySummaryResponse(
                        (Long) row[0],
                        (String) row[1],
                        (Long) row[2],
                        (String) row[3],
                        (BigDecimal) row[4]
                ))
                .toList();
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database"));
    }
}
