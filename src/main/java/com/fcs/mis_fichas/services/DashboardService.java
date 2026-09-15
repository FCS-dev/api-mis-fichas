package com.fcs.mis_fichas.services;

import com.fcs.mis_fichas.dtos.*;
import com.fcs.mis_fichas.entities.Transaction;
import com.fcs.mis_fichas.entities.User;
import com.fcs.mis_fichas.enums.Role;
import com.fcs.mis_fichas.enums.Type;
import com.fcs.mis_fichas.repositories.TransactionRepository;
import com.fcs.mis_fichas.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

    public List<MonthlyBalanceResponse> getMonthlyBalance(int months) {
        User user = getCurrentUser();
        LocalDate since = YearMonth.now().minusMonths(months - 1).atDay(1);
        List<Transaction> transactions = transactionRepository.findTransactionsSince(user.getId(), since);
        return computeMonthlyBalances(transactions, months);
    }

    public DashboardSummaryCardResponse getSummaryCard(int month, int year) {
        User user = getCurrentUser();
        LocalDate start = YearMonth.of(year, month).atDay(1);
        LocalDate end = YearMonth.of(year, month).atEndOfMonth();
        BigDecimal income = transactionRepository.sumByUserAndTypeBetweenDates(user.getId(), Type.INCOME, start, end);
        BigDecimal expense = transactionRepository.sumByUserAndTypeBetweenDates(user.getId(), Type.EXPENSE, start, end);
        BigDecimal balance = income.subtract(expense);
        double savingRate = income.compareTo(BigDecimal.ZERO) == 0 ? 0.0 :
                balance.multiply(BigDecimal.valueOf(100)).divide(income, 1, java.math.RoundingMode.HALF_UP).doubleValue();
        return new DashboardSummaryCardResponse(income, expense, balance, savingRate);
    }

    public MonthlyComparisonResponse getMonthlyComparison(Integer month, Integer year) {
        User user = getCurrentUser();
        YearMonth base = (month != null && year != null)
                ? YearMonth.of(year, month)
                : YearMonth.now();
        YearMonth previous = base.minusMonths(1);
        LocalDate curStart = base.atDay(1);
        LocalDate curEnd = base.atEndOfMonth();
        LocalDate prevStart = previous.atDay(1);
        LocalDate prevEnd = previous.atEndOfMonth();

        BigDecimal curIncome = transactionRepository.sumByUserAndTypeBetweenDates(user.getId(), Type.INCOME, curStart, curEnd);
        BigDecimal curExpense = transactionRepository.sumByUserAndTypeBetweenDates(user.getId(), Type.EXPENSE, curStart, curEnd);
        BigDecimal prevIncome = transactionRepository.sumByUserAndTypeBetweenDates(user.getId(), Type.INCOME, prevStart, prevEnd);
        BigDecimal prevExpense = transactionRepository.sumByUserAndTypeBetweenDates(user.getId(), Type.EXPENSE, prevStart, prevEnd);

        double expChange;
        if (prevExpense.compareTo(BigDecimal.ZERO) == 0) {
            expChange = curExpense.compareTo(BigDecimal.ZERO) == 0 ? 0.0 : 100.0;
        } else {
            expChange = curExpense.subtract(prevExpense).multiply(BigDecimal.valueOf(100))
                    .divide(prevExpense, 1, java.math.RoundingMode.HALF_UP).doubleValue();
        }

        double incChange;
        if (prevIncome.compareTo(BigDecimal.ZERO) == 0) {
            incChange = curIncome.compareTo(BigDecimal.ZERO) == 0 ? 0.0 : 100.0;
        } else {
            incChange = curIncome.subtract(prevIncome).multiply(BigDecimal.valueOf(100))
                    .divide(prevIncome, 1, java.math.RoundingMode.HALF_UP).doubleValue();
        }

        return new MonthlyComparisonResponse(
                formatExpenseGlossary(expChange),
                formatIncomeGlossary(incChange),
                expChange,
                incChange
        );
    }

    public TopExpensesResponse getTopExpenses(int month, int year) {
        User user = getCurrentUser();
        LocalDate start = YearMonth.of(year, month).atDay(1);
        LocalDate end = YearMonth.of(year, month).atEndOfMonth();

        List<Object[]> rawCats = transactionRepository.expenseSumGroupedByCategory(user.getId(), start, end);
        BigDecimal totalExpense = rawCats.stream()
                .map(r -> (BigDecimal) r[2])
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<TopExpenseEntry> topCats = rawCats.stream()
                .sorted((a, b) -> ((BigDecimal) b[2]).compareTo((BigDecimal) a[2]))
                .limit(3)
                .map(r -> {
                    BigDecimal amt = (BigDecimal) r[2];
                    double pct = totalExpense.compareTo(BigDecimal.ZERO) == 0 ? 0.0 :
                            amt.multiply(BigDecimal.valueOf(100)).divide(totalExpense, 1, java.math.RoundingMode.HALF_UP).doubleValue();
                    return new TopExpenseEntry((Long) r[0], (String) r[1], amt, pct);
                }).toList();

        List<Object[]> rawSubs = transactionRepository.expenseSumGroupedBySubcategoryAll(user.getId(), start, end);
        List<TopExpenseEntry> topSubs = rawSubs.stream()
                .limit(3)
                .map(r -> {
                    BigDecimal amt = (BigDecimal) r[2];
                    double pct = totalExpense.compareTo(BigDecimal.ZERO) == 0 ? 0.0 :
                            amt.multiply(BigDecimal.valueOf(100)).divide(totalExpense, 1, java.math.RoundingMode.HALF_UP).doubleValue();
                    return new TopExpenseEntry((Long) r[0], (String) r[1], amt, pct);
                }).toList();

        return new TopExpensesResponse(topCats, topSubs);
    }

    private String formatExpenseGlossary(double pct) {
        if (pct == 0) return "Tus gastos se mantuvieron igual que el mes pasado.";
        String abs = String.format("%.1f", Math.abs(pct));
        return pct < 0
                ? "Gastaste " + abs + "% menos que el mes pasado."
                : "Gastaste " + abs + "% más que el mes pasado.";
    }

    private String formatIncomeGlossary(double pct) {
        if (pct == 0) return "Tus ingresos se mantuvieron igual que el mes pasado.";
        String abs = String.format("%.1f", Math.abs(pct));
        return pct > 0
                ? "Tus ingresos aumentaron " + abs + "% respecto al mes anterior."
                : "Tus ingresos disminuyeron " + abs + "% respecto al mes anterior.";
    }

    public AdminStatsResponse getAdminStats() {
        long totalUsers = userRepository.countByDeletedAtIsNullAndRole(Role.USER);
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

    public List<MonthlyAverageResponse> getAvgIncome(Long userId) {
        return computeMonthlyAverages(Type.INCOME, userId);
    }

    public List<MonthlyAverageResponse> getAvgExpense(Long userId) {
        return computeMonthlyAverages(Type.EXPENSE, userId);
    }

    private List<MonthlyAverageResponse> computeMonthlyAverages(Type type, Long userId) {
        Long effectiveUserId = (userId != null && userId == 0L) ? null : userId;
        LocalDate since = YearMonth.now().minusMonths(MONTHS_HISTORY - 1).atDay(1);
        List<Transaction> transactions = transactionRepository.findTransactionsSince(effectiveUserId, since);
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

    public UserEvolutionResponse getUserEvolution(int monthFrom, int yearFrom, int monthTo, int yearTo) {
        LocalDate start = YearMonth.of(yearFrom, monthFrom).atDay(1);
        LocalDate end = YearMonth.of(yearTo, monthTo).atEndOfMonth();

        LocalDateTime startDt = start.atStartOfDay();
        LocalDateTime endDt = end.atTime(23, 59, 59);

        List<Object[]> registeredCurrent = userRepository.countRegisteredGroupedByMonth(startDt, endDt);
        List<Object[]> activeCurrent = transactionRepository.countDistinctUsersGroupedByMonth(start, end);

        Map<String, Long> newUsersCurrentMap = new LinkedHashMap<>();
        for (Object[] row : registeredCurrent) {
            int y = ((Number) row[0]).intValue();
            int m = ((Number) row[1]).intValue();
            long count = ((Number) row[2]).longValue();
            newUsersCurrentMap.put(y + "-" + m, count);
        }

        Map<String, Long> activeCurrentMap = new LinkedHashMap<>();
        for (Object[] row : activeCurrent) {
            activeCurrentMap.put(((Number) row[0]).intValue() + "-" + ((Number) row[1]).intValue(), ((Number) row[2]).longValue());
        }

        String firstKey = yearFrom + "-" + monthFrom;
        String lastKey = yearTo + "-" + monthTo;

        long firstActive = activeCurrentMap.getOrDefault(firstKey, 0L);
        long lastActive = activeCurrentMap.getOrDefault(lastKey, 0L);

        long accFirst = 0;
        long accLast = 0;
        YearMonth ymAcc = YearMonth.of(yearFrom, monthFrom);
        YearMonth ymAccEnd = YearMonth.of(yearTo, monthTo);
        while (!ymAcc.isAfter(ymAccEnd)) {
            String key = ymAcc.getYear() + "-" + ymAcc.getMonthValue();
            long newUsers = newUsersCurrentMap.getOrDefault(key, 0L);
            accLast += newUsers;
            if (ymAcc.equals(YearMonth.of(yearFrom, monthFrom))) {
                accFirst = accLast;
            }
            ymAcc = ymAcc.plusMonths(1);
        }

        long firstNew = newUsersCurrentMap.getOrDefault(firstKey, 0L);
        long lastNew = newUsersCurrentMap.getOrDefault(lastKey, 0L);

        UserEvolutionSummary summary = new UserEvolutionSummary(
                buildComparison(lastActive, firstActive),
                buildComparison(accLast, accFirst),
                buildComparison(lastNew, firstNew)
        );

        List<UserMonthlyData> monthly = new ArrayList<>();
        YearMonth ym = YearMonth.of(yearFrom, monthFrom);
        YearMonth ymEnd = YearMonth.of(yearTo, monthTo);
        long accumReg = 0;
        while (!ym.isAfter(ymEnd)) {
            String key = ym.getYear() + "-" + ym.getMonthValue();
            long newUsers = newUsersCurrentMap.getOrDefault(key, 0L);
            accumReg += newUsers;
            long active = activeCurrentMap.getOrDefault(key, 0L);
            monthly.add(new UserMonthlyData(ym.getYear(), ym.getMonthValue(), active, accumReg, newUsers));
            ym = ym.plusMonths(1);
        }

        return new UserEvolutionResponse(summary, monthly);
    }

    public TransactionEvolutionResponse getTransactionEvolution(int monthFrom, int yearFrom, int monthTo, int yearTo, Long userId) {
        Long effectiveUserId = (userId != null && userId == 0L) ? null : userId;
        LocalDate start = YearMonth.of(yearFrom, monthFrom).atDay(1);
        LocalDate end = YearMonth.of(yearTo, monthTo).atEndOfMonth();

        int monthsBetween = (yearTo - yearFrom) * 12 + (monthTo - monthFrom);
        LocalDate prevEnd = start.minusDays(1);
        LocalDate prevStart = prevEnd.minusMonths(monthsBetween).withDayOfMonth(1);

        List<Object[]> countCurrent = transactionRepository.countGroupedByMonthWithUser(effectiveUserId, start, end);
        List<Object[]> countPrev = transactionRepository.countGroupedByMonthWithUser(effectiveUserId, prevStart, prevEnd);
        List<Object[]> sumCurrent = transactionRepository.sumByTypeGroupedByMonthWithUser(effectiveUserId, start, end);
        List<Object[]> usersCurrent = transactionRepository.countDistinctUsersGroupedByMonthWithUser(effectiveUserId, start, end);
        List<Object[]> usersPrev = transactionRepository.countDistinctUsersGroupedByMonthWithUser(effectiveUserId, prevStart, prevEnd);

        Map<String, Long> countCurrentMap = new LinkedHashMap<>();
        Map<String, Long> countPrevMap = new LinkedHashMap<>();
        for (Object[] row : countCurrent) {
            countCurrentMap.put(((Number) row[0]).intValue() + "-" + ((Number) row[1]).intValue(), ((Number) row[2]).longValue());
        }
        for (Object[] row : countPrev) {
            countPrevMap.put(((Number) row[0]).intValue() + "-" + ((Number) row[1]).intValue(), ((Number) row[2]).longValue());
        }

        Map<String, Long> usersCurrentMap = new LinkedHashMap<>();
        Map<String, Long> usersPrevMap = new LinkedHashMap<>();
        for (Object[] row : usersCurrent) {
            usersCurrentMap.put(((Number) row[0]).intValue() + "-" + ((Number) row[1]).intValue(), ((Number) row[2]).longValue());
        }
        for (Object[] row : usersPrev) {
            usersPrevMap.put(((Number) row[0]).intValue() + "-" + ((Number) row[1]).intValue(), ((Number) row[2]).longValue());
        }

        Map<String, BigDecimal> incomeCurrentMap = new LinkedHashMap<>();
        Map<String, BigDecimal> expenseCurrentMap = new LinkedHashMap<>();
        for (Object[] row : sumCurrent) {
            String key = ((Number) row[0]).intValue() + "-" + ((Number) row[1]).intValue();
            Type type = (Type) row[2];
            BigDecimal total = (BigDecimal) row[3];
            if (type == Type.INCOME) {
                incomeCurrentMap.put(key, total);
            } else {
                expenseCurrentMap.put(key, total);
            }
        }

        long totalTxCurrent = countCurrentMap.values().stream().mapToLong(Long::longValue).sum();
        long totalTxPrev = countPrevMap.values().stream().mapToLong(Long::longValue).sum();
        long monthsCurrentCount = Math.max(1, countCurrentMap.size());
        long monthsPrevCount = Math.max(1, countPrevMap.size());
        BigDecimal avgTxCurrent = BigDecimal.valueOf(totalTxCurrent).divide(BigDecimal.valueOf(monthsCurrentCount), 2, java.math.RoundingMode.HALF_UP);
        BigDecimal avgTxPrev = BigDecimal.valueOf(totalTxPrev).divide(BigDecimal.valueOf(monthsPrevCount), 2, java.math.RoundingMode.HALF_UP);

        long totalUsersCurrent = usersCurrentMap.values().stream().mapToLong(Long::longValue).sum();
        long totalUsersPrev = usersPrevMap.values().stream().mapToLong(Long::longValue).sum();
        BigDecimal avgPerUserCurrent = totalUsersCurrent == 0 ? BigDecimal.ZERO :
                BigDecimal.valueOf(totalTxCurrent).divide(BigDecimal.valueOf(totalUsersCurrent), 2, java.math.RoundingMode.HALF_UP);
        BigDecimal avgPerUserPrev = totalUsersPrev == 0 ? BigDecimal.ZERO :
                BigDecimal.valueOf(totalTxPrev).divide(BigDecimal.valueOf(totalUsersPrev), 2, java.math.RoundingMode.HALF_UP);

        TransactionEvolutionSummary summary = new TransactionEvolutionSummary(
                buildDoubleComparison(avgTxCurrent, avgTxPrev),
                buildDoubleComparison(avgPerUserCurrent, avgPerUserPrev)
        );

        List<TransactionMonthlyData> monthly = new ArrayList<>();
        YearMonth ym = YearMonth.of(yearFrom, monthFrom);
        YearMonth ymEnd = YearMonth.of(yearTo, monthTo);
        while (!ym.isAfter(ymEnd)) {
            String key = ym.getYear() + "-" + ym.getMonthValue();
            long txCount = countCurrentMap.getOrDefault(key, 0L);
            long distinctUsers = usersCurrentMap.getOrDefault(key, 0L);
            BigDecimal avgPu = distinctUsers == 0 ? BigDecimal.ZERO :
                    BigDecimal.valueOf(txCount).divide(BigDecimal.valueOf(distinctUsers), 2, java.math.RoundingMode.HALF_UP);
            BigDecimal income = incomeCurrentMap.getOrDefault(key, BigDecimal.ZERO);
            BigDecimal expense = expenseCurrentMap.getOrDefault(key, BigDecimal.ZERO);
            monthly.add(new TransactionMonthlyData(ym.getYear(), ym.getMonthValue(), txCount, avgPu, income, expense));
            ym = ym.plusMonths(1);
        }

        return new TransactionEvolutionResponse(summary, monthly);
    }

    public MoneyMovementResponse getMoneyMovement(Long userId) {
        Long effectiveUserId = (userId != null && userId == 0L) ? null : userId;
        BigDecimal income = transactionRepository.sumByType(effectiveUserId, Type.INCOME);
        BigDecimal expense = transactionRepository.sumByType(effectiveUserId, Type.EXPENSE);

        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();
        LocalDate prevMonth = now.minusMonths(1);
        int prevYear = prevMonth.getYear();
        int prevMonthNum = prevMonth.getMonthValue();

        BigDecimal currentIncome = transactionRepository.sumByTypeInMonth(effectiveUserId, Type.INCOME, currentYear, currentMonth);
        BigDecimal prevIncome = transactionRepository.sumByTypeInMonth(effectiveUserId, Type.INCOME, prevYear, prevMonthNum);
        BigDecimal currentExpense = transactionRepository.sumByTypeInMonth(effectiveUserId, Type.EXPENSE, currentYear, currentMonth);
        BigDecimal prevExpense = transactionRepository.sumByTypeInMonth(effectiveUserId, Type.EXPENSE, prevYear, prevMonthNum);

        return new MoneyMovementResponse(
                income, expense, income.subtract(expense),
                currentIncome, prevIncome,
                currentExpense, prevExpense,
                currentIncome.subtract(currentExpense), prevIncome.subtract(prevExpense)
        );
    }

    public DashboardAveragesResponse getDashboardAverages(Long userId) {
        Long effectiveUserId = (userId != null && userId == 0L) ? null : userId;
        long totalUsers = userRepository.countByDeletedAtIsNullAndRoleNot(Role.ADMIN);
        if (totalUsers == 0) {
            return new DashboardAveragesResponse(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, null, null, null);
        }

        BigDecimal totalIncome = transactionRepository.sumByType(null, Type.INCOME);
        BigDecimal totalExpense = transactionRepository.sumByType(null, Type.EXPENSE);
        long totalTransactions = transactionRepository.countByDeletedAtIsNull();

        BigDecimal globalAvgIncome = totalUsers == 0 ? BigDecimal.ZERO :
                totalIncome.divide(BigDecimal.valueOf(totalUsers), 2, java.math.RoundingMode.HALF_UP);
        BigDecimal globalAvgExpense = totalUsers == 0 ? BigDecimal.ZERO :
                totalExpense.divide(BigDecimal.valueOf(totalUsers), 2, java.math.RoundingMode.HALF_UP);
        BigDecimal globalAvgTx = totalUsers == 0 ? BigDecimal.ZERO :
                BigDecimal.valueOf(totalTransactions).divide(BigDecimal.valueOf(totalUsers), 2, java.math.RoundingMode.HALF_UP);

        BigDecimal filteredIncome = null;
        BigDecimal filteredExpense = null;
        Long filteredUserId = null;
        if (effectiveUserId != null) {
            filteredUserId = effectiveUserId;
            BigDecimal sumIncome = transactionRepository.sumByType(effectiveUserId, Type.INCOME);
            BigDecimal sumExpense = transactionRepository.sumByType(effectiveUserId, Type.EXPENSE);

            Optional<User> userOpt = userRepository.findUserByIdAndDeletedAtIsNull(effectiveUserId);
            if (userOpt.isPresent()) {
                LocalDateTime createdAt = userOpt.get().getCreatedAt();
                YearMonth userStart = YearMonth.from(createdAt);
                YearMonth now = YearMonth.now();
                long monthsActive = java.time.temporal.ChronoUnit.MONTHS.between(userStart, now) + 1;
                monthsActive = Math.max(1, monthsActive);

                filteredIncome = sumIncome.divide(BigDecimal.valueOf(monthsActive), 2, java.math.RoundingMode.HALF_UP);
                filteredExpense = sumExpense.divide(BigDecimal.valueOf(monthsActive), 2, java.math.RoundingMode.HALF_UP);
            } else {
                filteredIncome = sumIncome;
                filteredExpense = sumExpense;
            }
        }

        return new DashboardAveragesResponse(globalAvgIncome, globalAvgExpense, globalAvgTx, filteredUserId, filteredIncome, filteredExpense);
    }

    public TopUsersResponse getTopUsers(int monthFrom, int yearFrom, int monthTo, int yearTo) {
        LocalDate start = YearMonth.of(yearFrom, monthFrom).atDay(1);
        LocalDate end = YearMonth.of(yearTo, monthTo).atEndOfMonth();
        Pageable top5 = PageRequest.of(0, 5);

        List<Object[]> byTx = transactionRepository.topUsersByTransactionCount(start, end, top5);
        List<Object[]> byExpense = transactionRepository.topUsersByTypeSum(Type.EXPENSE, start, end, top5);
        List<Object[]> byIncome = transactionRepository.topUsersByTypeSum(Type.INCOME, start, end, top5);

        return new TopUsersResponse(
                byTx.stream().map(r -> new TopUserEntry(((Number) r[0]).longValue(), (String) r[1], BigDecimal.valueOf(((Number) r[2]).longValue()))).toList(),
                byExpense.stream().map(r -> new TopUserEntry(((Number) r[0]).longValue(), (String) r[1], (BigDecimal) r[2])).toList(),
                byIncome.stream().map(r -> new TopUserEntry(((Number) r[0]).longValue(), (String) r[1], (BigDecimal) r[2])).toList()
        );
    }

    public ActivityDistributionResponse getActivityDistribution(int month, int year) {
        LocalDate start = YearMonth.of(year, month).atDay(1);
        LocalDate end = YearMonth.of(year, month).atEndOfMonth();
        LocalDateTime startDt = start.atStartOfDay();
        LocalDateTime endDt = end.atTime(23, 59, 59);

        List<Object[]> perUser = transactionRepository.countPerUserInMonth(start, end);
        long registeredInMonth = userRepository.countRegisteredInMonth(startDt, endDt);

        long frecuente = 0, regular = 0, ocasional = 0;
        for (Object[] row : perUser) {
            long count = ((Number) row[1]).longValue();
            if (count > 20) frecuente++;
            else if (count >= 5) regular++;
            else ocasional++;
        }
        long totalEvaluated = frecuente + regular + ocasional;
        long inactivo = Math.max(0, registeredInMonth - totalEvaluated);

        return new ActivityDistributionResponse(
                buildActivityCategory(frecuente, totalEvaluated),
                buildActivityCategory(regular, totalEvaluated),
                buildActivityCategory(ocasional, totalEvaluated),
                buildActivityCategory(inactivo, totalEvaluated)
        );
    }

    private List<MonthlyBalanceResponse> computeMonthlyBalances(List<Transaction> transactions, int months) {
        Map<YearMonth, List<Transaction>> grouped = transactions.stream()
                .collect(Collectors.groupingBy(t -> YearMonth.from(t.getTransactionDate())));
        List<MonthlyBalanceResponse> result = new ArrayList<>();
        YearMonth start = YearMonth.now().minusMonths(months - 1);
        for (int i = 0; i < months; i++) {
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
            double savingRate = income.compareTo(BigDecimal.ZERO) == 0 ? 0.0 :
                    balance.multiply(BigDecimal.valueOf(100)).divide(income, 1, java.math.RoundingMode.HALF_UP).doubleValue();
            result.add(new MonthlyBalanceResponse(ym.getYear(), ym.getMonthValue(), income, expense, balance, savingRate));
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

    private PeriodComparison buildComparison(long current, long previous) {
        double changePercent = previous == 0 ? (current == 0 ? 0 : 100.0) :
                ((double) (current - previous) / previous) * 100.0;
        return new PeriodComparison(current, previous, Math.round(changePercent * 10.0) / 10.0);
    }

    private PeriodComparisonDouble buildDoubleComparison(BigDecimal current, BigDecimal previous) {
        double changePercent = previous.compareTo(BigDecimal.ZERO) == 0 ?
                (current.compareTo(BigDecimal.ZERO) == 0 ? 0.0 : 100.0) :
                current.subtract(previous).multiply(BigDecimal.valueOf(100))
                        .divide(previous, 1, java.math.RoundingMode.HALF_UP).doubleValue();
        return new PeriodComparisonDouble(current, previous, Math.round(changePercent * 10.0) / 10.0);
    }

    private ActivityCategory buildActivityCategory(long count, long totalUsers) {
        double percentage = totalUsers == 0 ? 0.0 :
                Math.round(((double) count / totalUsers) * 1000.0) / 10.0;
        return new ActivityCategory(count, percentage);
    }
}
