package com.spendwise.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class MonthlyReportResponse {
    private int year;
    private int month;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal balance;
    private long transactionCount;
    private List<CategoryBreakdownItem> expenseByCategory;
    private List<CategoryBreakdownItem> incomeByCategory;

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(BigDecimal totalIncome) {
        this.totalIncome = totalIncome;
    }

    public BigDecimal getTotalExpenses() {
        return totalExpenses;
    }

    public void setTotalExpenses(BigDecimal totalExpenses) {
        this.totalExpenses = totalExpenses;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public long getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(long transactionCount) {
        this.transactionCount = transactionCount;
    }

    public List<CategoryBreakdownItem> getExpenseByCategory() {
        return expenseByCategory;
    }

    public void setExpenseByCategory(List<CategoryBreakdownItem> expenseByCategory) {
        this.expenseByCategory = expenseByCategory;
    }

    public List<CategoryBreakdownItem> getIncomeByCategory() {
        return incomeByCategory;
    }

    public void setIncomeByCategory(List<CategoryBreakdownItem> incomeByCategory) {
        this.incomeByCategory = incomeByCategory;
    }
}
