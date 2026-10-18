package com.spendwise.service.impl;

import com.spendwise.dto.MonthlyReportResponse;
import com.spendwise.dto.MonthlyTrendResponse;
import com.spendwise.dto.YearlyReportResponse;
import com.spendwise.entity.Transaction;
import com.spendwise.entity.TransactionType;
import com.spendwise.entity.User;
import com.spendwise.repository.TransactionRepository;
import com.spendwise.service.CurrentUserProvider;
import com.spendwise.service.ReportService;
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
public class ReportServiceImpl implements ReportService {
    private final TransactionRepository transactionRepository;
    private final CurrentUserProvider currentUserProvider;
    private final TransactionAnalytics analytics;

    public ReportServiceImpl(TransactionRepository transactionRepository,
                             CurrentUserProvider currentUserProvider,
                             TransactionAnalytics analytics) {
        this.transactionRepository = transactionRepository;
        this.currentUserProvider = currentUserProvider;
        this.analytics = analytics;
    }

    @Override
    @Transactional(readOnly = true)
    public MonthlyReportResponse getMonthlyReport(Integer year, Integer month) {
        User user = currentUserProvider.getCurrentUser();
        LocalDate today = LocalDate.now();
        int resolvedYear = year != null ? year : today.getYear();
        int resolvedMonth = month != null ? month : today.getMonthValue();
        ReportPeriod period = ReportPeriod.month(resolvedYear, resolvedMonth);
        List<Transaction> transactions = fetch(user.getUserId(), period);

        BigDecimal income = analytics.sumByType(transactions, TransactionType.INCOME);
        BigDecimal expenses = analytics.sumByType(transactions, TransactionType.EXPENSE);

        MonthlyReportResponse response = new MonthlyReportResponse();
        response.setYear(resolvedYear);
        response.setMonth(resolvedMonth);
        response.setStartDate(period.startDate());
        response.setEndDate(period.endDate());
        response.setTotalIncome(income);
        response.setTotalExpenses(expenses);
        response.setBalance(income.subtract(expenses));
        response.setTransactionCount(transactions.size());
        response.setExpenseByCategory(analytics.breakdown(transactions, TransactionType.EXPENSE));
        response.setIncomeByCategory(analytics.breakdown(transactions, TransactionType.INCOME));
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public YearlyReportResponse getYearlyReport(Integer year) {
        User user = currentUserProvider.getCurrentUser();
        int resolvedYear = year != null ? year : LocalDate.now().getYear();
        ReportPeriod period = ReportPeriod.year(resolvedYear);
        List<Transaction> transactions = fetch(user.getUserId(), period);

        Map<YearMonth, List<Transaction>> grouped = transactions.stream()
                .collect(Collectors.groupingBy(transaction -> YearMonth.from(transaction.getTransactionDate())));

        List<MonthlyTrendResponse> monthlyBreakdown = new ArrayList<>();
        for (int monthValue = 1; monthValue <= 12; monthValue++) {
            YearMonth yearMonth = YearMonth.of(resolvedYear, monthValue);
            monthlyBreakdown.add(analytics.toMonthlyTrend(yearMonth, grouped.getOrDefault(yearMonth, List.of())));
        }

        BigDecimal income = analytics.sumByType(transactions, TransactionType.INCOME);
        BigDecimal expenses = analytics.sumByType(transactions, TransactionType.EXPENSE);

        YearlyReportResponse response = new YearlyReportResponse();
        response.setYear(resolvedYear);
        response.setStartDate(period.startDate());
        response.setEndDate(period.endDate());
        response.setTotalIncome(income);
        response.setTotalExpenses(expenses);
        response.setBalance(income.subtract(expenses));
        response.setTransactionCount(transactions.size());
        response.setMonthlyBreakdown(monthlyBreakdown);
        response.setExpenseByCategory(analytics.breakdown(transactions, TransactionType.EXPENSE));
        response.setIncomeByCategory(analytics.breakdown(transactions, TransactionType.INCOME));
        return response;
    }

    private List<Transaction> fetch(Long userId, ReportPeriod period) {
        return transactionRepository.findByUserUserIdAndTransactionDateBetween(
                userId, period.startDate(), period.endDate());
    }
}
