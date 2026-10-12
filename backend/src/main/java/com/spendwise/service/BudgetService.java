package com.spendwise.service;

import com.spendwise.dto.BudgetRequest;
import com.spendwise.dto.BudgetResponse;

import java.util.List;

public interface BudgetService {
    List<BudgetResponse> getAll();
    BudgetResponse getById(Long id);
    BudgetResponse create(BudgetRequest request);
    BudgetResponse update(Long id, BudgetRequest request);
    void delete(Long id);
}
