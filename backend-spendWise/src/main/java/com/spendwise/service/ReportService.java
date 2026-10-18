package com.spendwise.service;

import com.spendwise.dto.MonthlyReportResponse;
import com.spendwise.dto.YearlyReportResponse;

public interface ReportService {
    MonthlyReportResponse getMonthlyReport(Integer year, Integer month);
    YearlyReportResponse getYearlyReport(Integer year);
}
