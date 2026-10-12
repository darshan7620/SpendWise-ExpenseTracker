package com.spendwise.controller;

import com.spendwise.dto.CategoryBreakdownResponse;
import com.spendwise.dto.DashboardSummaryResponse;
import com.spendwise.dto.MonthlyTrendResponse;
import com.spendwise.entity.TransactionType;
import com.spendwise.service.DashboardService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
@Validated
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public DashboardSummaryResponse getSummary(
            @RequestParam(required = false) @Min(1900) @Max(2200) Integer year,
            @RequestParam(required = false) @Min(1) @Max(12) Integer month) {
        return dashboardService.getSummary(year, month);
    }

    @GetMapping("/monthly-trends")
    public List<MonthlyTrendResponse> getMonthlyTrends(
            @RequestParam(defaultValue = "6") @Min(1) @Max(24) int months) {
        return dashboardService.getMonthlyTrends(months);
    }

    @GetMapping("/category-breakdown")
    public CategoryBreakdownResponse getCategoryBreakdown(
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) @Min(1900) @Max(2200) Integer year,
            @RequestParam(required = false) @Min(1) @Max(12) Integer month) {
        return dashboardService.getCategoryBreakdown(type, year, month);
    }
}
