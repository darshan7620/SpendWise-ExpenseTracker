package com.spendwise.service;

import com.spendwise.dto.SavingsGoalProgressRequest;
import com.spendwise.dto.SavingsGoalRequest;
import com.spendwise.dto.SavingsGoalResponse;

import java.util.List;

public interface SavingsGoalService {
    List<SavingsGoalResponse> getAll();
    SavingsGoalResponse getById(Long id);
    SavingsGoalResponse create(SavingsGoalRequest request);
    SavingsGoalResponse update(Long id, SavingsGoalRequest request);
    SavingsGoalResponse updateProgress(Long id, SavingsGoalProgressRequest request);
    void delete(Long id);
}
