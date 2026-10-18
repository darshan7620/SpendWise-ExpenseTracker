package com.spendwise.dto;

import com.spendwise.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class CategoryBreakdownResponse {
    private TransactionType type;
    private int year;
    private Integer month;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalAmount;
    private List<CategoryBreakdownItem> categories;

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
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

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public List<CategoryBreakdownItem> getCategories() {
        return categories;
    }

    public void setCategories(List<CategoryBreakdownItem> categories) {
        this.categories = categories;
    }
}
