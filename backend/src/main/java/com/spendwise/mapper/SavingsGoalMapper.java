package com.spendwise.mapper;

import com.spendwise.dto.SavingsGoalRequest;
import com.spendwise.dto.SavingsGoalResponse;
import com.spendwise.entity.SavingsGoal;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class SavingsGoalMapper {
    public SavingsGoal toEntity(SavingsGoalRequest request) {
        SavingsGoal goal = new SavingsGoal();
        goal.setGoalName(request.getGoalName());
        goal.setTargetAmount(request.getTargetAmount());
        goal.setCurrentAmount(request.getCurrentAmount() != null ? request.getCurrentAmount() : BigDecimal.ZERO);
        goal.setTargetDate(request.getTargetDate());
        return goal;
    }

    public void updateEntity(SavingsGoalRequest request, SavingsGoal goal) {
        goal.setGoalName(request.getGoalName());
        goal.setTargetAmount(request.getTargetAmount());
        if (request.getCurrentAmount() != null) {
            goal.setCurrentAmount(request.getCurrentAmount());
        }
        goal.setTargetDate(request.getTargetDate());
    }

    public SavingsGoalResponse toResponse(SavingsGoal goal) {
        SavingsGoalResponse response = new SavingsGoalResponse();
        response.setGoalId(goal.getGoalId());
        response.setGoalName(goal.getGoalName());
        response.setTargetAmount(goal.getTargetAmount());
        response.setCurrentAmount(goal.getCurrentAmount());
        BigDecimal progress = BigDecimal.ZERO;
        if (goal.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
            progress = goal.getCurrentAmount().divide(goal.getTargetAmount(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }
        response.setProgressPercentage(progress.setScale(2, RoundingMode.HALF_UP));
        BigDecimal remaining = goal.getTargetAmount().subtract(goal.getCurrentAmount());
        if (remaining.compareTo(BigDecimal.ZERO) < 0) {
            remaining = BigDecimal.ZERO;
        }
        response.setRemainingAmount(remaining.setScale(2, RoundingMode.HALF_UP));
        response.setTargetDate(goal.getTargetDate());
        response.setStatus(goal.getStatus());
        response.setUserId(goal.getUser() != null ? goal.getUser().getUserId() : null);
        response.setCreatedAt(goal.getCreatedAt());
        return response;
    }
}
