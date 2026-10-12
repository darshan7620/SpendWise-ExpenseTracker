package com.spendwise.service;

import com.spendwise.dto.CategoryBreakdownResponse;
import com.spendwise.dto.DashboardSummaryResponse;
import com.spendwise.dto.MonthlyTrendResponse;
import com.spendwise.entity.TransactionType;

import java.util.List;

public interface DashboardService {
    DashboardSummaryResponse getSummary(Integer year, Integer month);
    List<MonthlyTrendResponse> getMonthlyTrends(int months);
    CategoryBreakdownResponse getCategoryBreakdown(TransactionType type, Integer year, Integer month);
}
