package com.spendwise.service.impl;

import com.spendwise.dto.CategoryBreakdownItem;
import com.spendwise.dto.CategoryBreakdownResponse;
import com.spendwise.dto.DashboardSummaryResponse;
import com.spendwise.dto.MonthlyTrendResponse;
import com.spendwise.entity.Transaction;
import com.spendwise.entity.TransactionType;
import com.spendwise.entity.User;
import com.spendwise.repository.TransactionRepository;
import com.spendwise.service.CurrentUserProvider;
import com.spendwise.service.DashboardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {
    private final TransactionRepository transactionRepository;
    private final CurrentUserProvider currentUserProvider;
    private final TransactionAnalytics analytics;

    public DashboardServiceImpl(TransactionRepository transactionRepository,
                                CurrentUserProvider currentUserProvider,
                                TransactionAnalytics analytics) {
        this.transactionRepository = transactionRepository;
        this.currentUserProvider = currentUserProvider;
        this.analytics = analytics;
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary(Integer year, Integer month) {
        User user = currentUserProvider.getCurrentUser();
        ReportPeriod period = ReportPeriod.resolve(year, month);
        List<Transaction> transactions = fetch(user.getUserId(), period);
        return buildSummary(period, transactions);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MonthlyTrendResponse> getMonthlyTrends(int months) {
        User user = currentUserProvider.getCurrentUser();
        YearMonth end = YearMonth.now();
        YearMonth start = end.minusMonths(months - 1L);
        List<Transaction> transactions = transactionRepository.findByUserUserIdAndTransactionDateBetween(
                user.getUserId(), start.atDay(1), end.atEndOfMonth());

        Map<YearMonth, List<Transaction>> grouped = transactions.stream()
                .collect(Collectors.groupingBy(transaction -> YearMonth.from(transaction.getTransactionDate())));

        List<MonthlyTrendResponse> trends = new ArrayList<>();
        for (YearMonth cursor = start; !cursor.isAfter(end); cursor = cursor.plusMonths(1)) {
            trends.add(analytics.toMonthlyTrend(cursor, grouped.getOrDefault(cursor, List.of())));
        }
        return trends;
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryBreakdownResponse getCategoryBreakdown(TransactionType type, Integer year, Integer month) {
        User user = currentUserProvider.getCurrentUser();
        TransactionType resolvedType = type != null ? type : TransactionType.EXPENSE;
        ReportPeriod period = ReportPeriod.resolve(year, month);
        List<Transaction> transactions = fetch(user.getUserId(), period);
        List<CategoryBreakdownItem> categories = analytics.breakdown(transactions, resolvedType);

        CategoryBreakdownResponse response = new CategoryBreakdownResponse();
        response.setType(resolvedType);
        response.setYear(period.year());
        response.setMonth(period.month());
        response.setStartDate(period.startDate());
        response.setEndDate(period.endDate());
        response.setTotalAmount(analytics.sumByType(transactions, resolvedType));
        response.setCategories(categories);
        return response;
    }

    private List<Transaction> fetch(Long userId, ReportPeriod period) {
        return transactionRepository.findByUserUserIdAndTransactionDateBetween(
                userId, period.startDate(), period.endDate());
    }

    private DashboardSummaryResponse buildSummary(ReportPeriod period, List<Transaction> transactions) {
        BigDecimal income = analytics.sumByType(transactions, TransactionType.INCOME);
        BigDecimal expenses = analytics.sumByType(transactions, TransactionType.EXPENSE);

        DashboardSummaryResponse response = new DashboardSummaryResponse();
        response.setYear(period.year());
        response.setMonth(period.month());
        response.setStartDate(period.startDate());
        response.setEndDate(period.endDate());
        response.setTotalIncome(income);
        response.setTotalExpenses(expenses);
        response.setBalance(income.subtract(expenses));
        response.setTransactionCount(transactions.size());
        response.setIncomeCount(analytics.countByType(transactions, TransactionType.INCOME));
        response.setExpenseCount(analytics.countByType(transactions, TransactionType.EXPENSE));
        return response;
    }
}
