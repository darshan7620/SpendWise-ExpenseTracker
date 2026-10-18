package com.spendwise.service.impl;

import com.spendwise.dto.SavingsGoalProgressRequest;
import com.spendwise.dto.SavingsGoalRequest;
import com.spendwise.dto.SavingsGoalResponse;
import com.spendwise.entity.GoalStatus;
import com.spendwise.entity.SavingsGoal;
import com.spendwise.entity.User;
import com.spendwise.exception.BadRequestException;
import com.spendwise.exception.ForbiddenException;
import com.spendwise.exception.ResourceNotFoundException;
import com.spendwise.mapper.SavingsGoalMapper;
import com.spendwise.repository.SavingsGoalRepository;
import com.spendwise.service.CurrentUserProvider;
import com.spendwise.service.SavingsGoalService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SavingsGoalServiceImpl implements SavingsGoalService {
    private final SavingsGoalRepository savingsGoalRepository;
    private final SavingsGoalMapper mapper;
    private final CurrentUserProvider currentUserProvider;

    public SavingsGoalServiceImpl(SavingsGoalRepository savingsGoalRepository,
                                 SavingsGoalMapper mapper,
                                 CurrentUserProvider currentUserProvider) {
        this.savingsGoalRepository = savingsGoalRepository;
        this.mapper = mapper;
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SavingsGoalResponse> getAll() {
        User currentUser = currentUserProvider.getCurrentUser();
        return savingsGoalRepository.findByUserUserId(currentUser.getUserId()).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SavingsGoalResponse getById(Long id) {
        SavingsGoal goal = savingsGoalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Savings goal not found"));
        User currentUser = currentUserProvider.getCurrentUser();
        if (!goal.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new ForbiddenException("Cannot access this goal");
        }
        return mapper.toResponse(goal);
    }

    @Override
    @Transactional
    public SavingsGoalResponse create(SavingsGoalRequest request) {
        User currentUser = currentUserProvider.getCurrentUser();
        SavingsGoal goal = mapper.toEntity(request);
        if (goal.getCurrentAmount().compareTo(goal.getTargetAmount()) > 0) {
            goal.setCurrentAmount(goal.getTargetAmount());
            goal.setStatus(GoalStatus.COMPLETED);
        }
        goal.setUser(currentUser);
        return mapper.toResponse(savingsGoalRepository.save(goal));
    }

    @Override
    @Transactional
    public SavingsGoalResponse update(Long id, SavingsGoalRequest request) {
        SavingsGoal goal = savingsGoalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Savings goal not found"));
        User currentUser = currentUserProvider.getCurrentUser();
        if (!goal.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new ForbiddenException("Cannot modify this goal");
        }
        mapper.updateEntity(request, goal);
        if (goal.getCurrentAmount().compareTo(goal.getTargetAmount()) >= 0) {
            goal.setCurrentAmount(goal.getTargetAmount());
            goal.setStatus(GoalStatus.COMPLETED);
        } else if (goal.getStatus() == GoalStatus.CANCELLED) {
            throw new BadRequestException("Cannot modify cancelled goal without explicit operation");
        } else {
            goal.setStatus(GoalStatus.IN_PROGRESS);
        }
        return mapper.toResponse(savingsGoalRepository.save(goal));
    }

    @Override
    @Transactional
    public SavingsGoalResponse updateProgress(Long id, SavingsGoalProgressRequest request) {
        SavingsGoal goal = savingsGoalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Savings goal not found"));
        User currentUser = currentUserProvider.getCurrentUser();
        if (!goal.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new ForbiddenException("Cannot modify this goal");
        }
        if (goal.getStatus() == GoalStatus.CANCELLED) {
            throw new BadRequestException("Cannot update progress on cancelled goal");
        }
        BigDecimal newAmount = goal.getCurrentAmount().add(request.getAmountToAdd());
        if (newAmount.compareTo(goal.getTargetAmount()) >= 0) {
            goal.setCurrentAmount(goal.getTargetAmount());
            goal.setStatus(GoalStatus.COMPLETED);
        } else {
            goal.setCurrentAmount(newAmount);
            goal.setStatus(GoalStatus.IN_PROGRESS);
        }
        return mapper.toResponse(savingsGoalRepository.save(goal));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        SavingsGoal goal = savingsGoalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Savings goal not found"));
        User currentUser = currentUserProvider.getCurrentUser();
        if (!goal.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new ForbiddenException("Cannot delete this goal");
        }
        savingsGoalRepository.delete(goal);
    }
}
