package com.spendwise.service.impl;

import com.spendwise.dto.CategoryBreakdownItem;
import com.spendwise.dto.MonthlyTrendResponse;
import com.spendwise.entity.Transaction;
import com.spendwise.entity.TransactionType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TransactionAnalytics {

    public BigDecimal sumByType(List<Transaction> transactions, TransactionType type) {
        return transactions.stream()
                .filter(transaction -> transaction.getType() == type)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public long countByType(List<Transaction> transactions, TransactionType type) {
        return transactions.stream()
                .filter(transaction -> transaction.getType() == type)
                .count();
    }

    public MonthlyTrendResponse toMonthlyTrend(YearMonth yearMonth, List<Transaction> transactions) {
        BigDecimal income = sumByType(transactions, TransactionType.INCOME);
        BigDecimal expenses = sumByType(transactions, TransactionType.EXPENSE);

        MonthlyTrendResponse response = new MonthlyTrendResponse();
        response.setYear(yearMonth.getYear());
        response.setMonth(yearMonth.getMonthValue());
        response.setLabel(yearMonth.toString());
        response.setTotalIncome(income);
        response.setTotalExpenses(expenses);
        response.setBalance(income.subtract(expenses));
        response.setTransactionCount(transactions.size());
        return response;
    }

    public List<CategoryBreakdownItem> breakdown(List<Transaction> transactions, TransactionType type) {
        List<Transaction> filtered = transactions.stream()
                .filter(transaction -> transaction.getType() == type)
                .toList();

        BigDecimal total = filtered.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<Long, CategoryAccumulator> grouped = new HashMap<>();
        for (Transaction transaction : filtered) {
            Long categoryId = transaction.getCategory() != null ? transaction.getCategory().getCategoryId() : null;
            String categoryName = transaction.getCategory() != null
                    ? transaction.getCategory().getCategoryName()
                    : "Uncategorized";
            CategoryAccumulator accumulator = grouped.computeIfAbsent(categoryId, key -> new CategoryAccumulator(categoryName));
            accumulator.add(transaction.getAmount());
        }

        List<CategoryBreakdownItem> items = new ArrayList<>();
        for (Map.Entry<Long, CategoryAccumulator> entry : grouped.entrySet()) {
            CategoryBreakdownItem item = new CategoryBreakdownItem();
            item.setCategoryId(entry.getKey());
            item.setCategoryName(entry.getValue().name);
            item.setTotalAmount(entry.getValue().total);
            item.setTransactionCount(entry.getValue().count);
            item.setPercentage(percentage(entry.getValue().total, total));
            items.add(item);
        }

        items.sort(Comparator.comparing(CategoryBreakdownItem::getTotalAmount).reversed()
                .thenComparing(item -> item.getCategoryName() == null ? "" : item.getCategoryName()));
        return items;
    }

    private BigDecimal percentage(BigDecimal part, BigDecimal total) {
        if (total.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return part.multiply(BigDecimal.valueOf(100)).divide(total, 2, RoundingMode.HALF_UP);
    }

    private static final class CategoryAccumulator {
        private final String name;
        private BigDecimal total = BigDecimal.ZERO;
        private long count;

        private CategoryAccumulator(String name) {
            this.name = name;
        }

        private void add(BigDecimal amount) {
            total = total.add(amount);
            count++;
        }
    }
}
