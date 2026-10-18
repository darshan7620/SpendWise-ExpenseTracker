package com.spendwise.controller;

import com.spendwise.dto.MonthlyReportResponse;
import com.spendwise.dto.YearlyReportResponse;
import com.spendwise.service.ReportService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
@Validated
public class ReportController {
    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/monthly")
    public MonthlyReportResponse getMonthlyReport(
            @RequestParam(required = false) @Min(1900) @Max(2200) Integer year,
            @RequestParam(required = false) @Min(1) @Max(12) Integer month) {
        return reportService.getMonthlyReport(year, month);
    }

    @GetMapping("/yearly")
    public YearlyReportResponse getYearlyReport(
            @RequestParam(required = false) @Min(1900) @Max(2200) Integer year) {
        return reportService.getYearlyReport(year);
    }
}
