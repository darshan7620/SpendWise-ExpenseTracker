package com.spendwise.service.impl;

import java.time.LocalDate;
import java.time.YearMonth;

record ReportPeriod(int year, Integer month, LocalDate startDate, LocalDate endDate) {

    boolean isMonthly() {
        return month != null;
    }

    static ReportPeriod month(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        return new ReportPeriod(year, month, yearMonth.atDay(1), yearMonth.atEndOfMonth());
    }

    static ReportPeriod year(int year) {
        return new ReportPeriod(year, null, LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31));
    }

    static ReportPeriod resolve(Integer year, Integer month) {
        if (year == null && month == null) {
            LocalDate today = LocalDate.now();
            return month(today.getYear(), today.getMonthValue());
        }
        int resolvedYear = year != null ? year : LocalDate.now().getYear();
        if (month == null) {
            return year(resolvedYear);
        }
        return month(resolvedYear, month);
    }
}
