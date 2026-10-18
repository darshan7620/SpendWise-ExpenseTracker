package com.spendwise.service.impl;

import com.spendwise.dto.BudgetRequest;
import com.spendwise.dto.BudgetResponse;
import com.spendwise.entity.Budget;
import com.spendwise.entity.Category;
import com.spendwise.entity.Transaction;
import com.spendwise.entity.TransactionType;
import com.spendwise.entity.User;
import com.spendwise.exception.DuplicateResourceException;
import com.spendwise.exception.ForbiddenException;
import com.spendwise.exception.ResourceNotFoundException;
import com.spendwise.mapper.BudgetMapper;
import com.spendwise.repository.BudgetRepository;
import com.spendwise.repository.CategoryRepository;
import com.spendwise.repository.TransactionRepository;
import com.spendwise.service.BudgetService;
import com.spendwise.service.CurrentUserProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BudgetServiceImpl implements BudgetService {
    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final BudgetMapper mapper;
    private final CurrentUserProvider currentUserProvider;

    public BudgetServiceImpl(BudgetRepository budgetRepository,
                             CategoryRepository categoryRepository,
                             TransactionRepository transactionRepository,
                             BudgetMapper mapper,
                             CurrentUserProvider currentUserProvider) {
        this.budgetRepository = budgetRepository;
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
        this.mapper = mapper;
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BudgetResponse> getAll() {
        User currentUser = currentUserProvider.getCurrentUser();
        return budgetRepository.findByUserUserId(currentUser.getUserId()).stream()
                .map(budget -> mapper.toResponse(budget, calculateSpent(budget)))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BudgetResponse getById(Long id) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));
        User currentUser = currentUserProvider.getCurrentUser();
        if (!budget.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new ForbiddenException("Cannot access this budget");
        }
        return mapper.toResponse(budget, calculateSpent(budget));
    }

    @Override
    @Transactional
    public BudgetResponse create(BudgetRequest request) {
        User currentUser = currentUserProvider.getCurrentUser();
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        if (budgetRepository.existsByUserUserIdAndCategoryCategoryIdAndMonthAndYear(currentUser.getUserId(),
                request.getCategoryId(), request.getMonth(), request.getYear())) {
            throw new DuplicateResourceException("Budget already exists");
        }
        Budget budget = mapper.toEntity(request);
        budget.setUser(currentUser);
        budget.setCategory(category);
        Budget saved = budgetRepository.save(budget);
        return mapper.toResponse(saved, BigDecimal.ZERO);
    }

    @Override
    @Transactional
    public BudgetResponse update(Long id, BudgetRequest request) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));
        User currentUser = currentUserProvider.getCurrentUser();
        if (!budget.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new ForbiddenException("Cannot modify this budget");
        }
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        budgetRepository.findByUserUserIdAndCategoryCategoryIdAndMonthAndYear(currentUser.getUserId(),
                request.getCategoryId(), request.getMonth(), request.getYear()).ifPresent(existing -> {
            if (!existing.getBudgetId().equals(id)) {
                throw new DuplicateResourceException("Budget already exists");
            }
        });
        mapper.updateEntity(request, budget);
        budget.setCategory(category);
        Budget saved = budgetRepository.save(budget);
        return mapper.toResponse(saved, calculateSpent(budget));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));
        User currentUser = currentUserProvider.getCurrentUser();
        if (!budget.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new ForbiddenException("Cannot delete this budget");
        }
        budgetRepository.delete(budget);
    }

    private BigDecimal calculateSpent(Budget budget) {
        LocalDate start = LocalDate.of(budget.getYear(), budget.getMonth(), 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        List<Transaction> transactions = transactionRepository.findByUserUserIdAndTransactionDateBetween(
                budget.getUser().getUserId(), start, end);
        return transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .filter(t -> t.getCategory() != null && t.getCategory().getCategoryId().equals(budget.getCategory().getCategoryId()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
