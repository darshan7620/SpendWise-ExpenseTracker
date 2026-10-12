package com.spendwise.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class SavingsGoalProgressRequest {
    @NotNull
    @DecimalMin(value = "0.00", inclusive = true)
    private BigDecimal amountToAdd;

    public BigDecimal getAmountToAdd() {
        return amountToAdd;
    }

    public void setAmountToAdd(BigDecimal amountToAdd) {
        this.amountToAdd = amountToAdd;
    }
}
