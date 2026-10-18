package com.spendwise.mapper;

import com.spendwise.dto.BudgetRequest;
import com.spendwise.dto.BudgetResponse;
import com.spendwise.entity.Budget;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class BudgetMapper {
    public Budget toEntity(BudgetRequest request) {
        Budget budget = new Budget();
        budget.setLimitAmount(request.getLimitAmount());
        budget.setMonth(request.getMonth());
        budget.setYear(request.getYear());
        return budget;
    }

    public void updateEntity(BudgetRequest request, Budget budget) {
        budget.setLimitAmount(request.getLimitAmount());
        budget.setMonth(request.getMonth());
        budget.setYear(request.getYear());
    }

    public BudgetResponse toResponse(Budget budget, BigDecimal spentAmount) {
        BudgetResponse response = new BudgetResponse();
        response.setBudgetId(budget.getBudgetId());
        response.setLimitAmount(budget.getLimitAmount());
        BigDecimal spent = spentAmount == null ? BigDecimal.ZERO : spentAmount;
        response.setSpentAmount(spent.setScale(2, RoundingMode.HALF_UP));
        BigDecimal remaining = budget.getLimitAmount().subtract(spent);
        if (remaining.compareTo(BigDecimal.ZERO) < 0) {
            remaining = BigDecimal.ZERO;
        }
        response.setRemainingAmount(remaining.setScale(2, RoundingMode.HALF_UP));
        BigDecimal percentage = BigDecimal.ZERO;
        if (budget.getLimitAmount().compareTo(BigDecimal.ZERO) > 0) {
            percentage = spent.divide(budget.getLimitAmount(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }
        response.setPercentageUsed(percentage.setScale(2, RoundingMode.HALF_UP));
        if (spent.compareTo(budget.getLimitAmount()) >= 0) {
            response.setWarningStatus("EXCEEDED");
        } else if (percentage.compareTo(BigDecimal.valueOf(80)) >= 0) {
            response.setWarningStatus("WARNING");
        } else {
            response.setWarningStatus("NORMAL");
        }
        response.setMonth(budget.getMonth());
        response.setYear(budget.getYear());
        response.setUserId(budget.getUser() != null ? budget.getUser().getUserId() : null);
        response.setCategoryId(budget.getCategory() != null ? budget.getCategory().getCategoryId() : null);
        return response;
    }
}
