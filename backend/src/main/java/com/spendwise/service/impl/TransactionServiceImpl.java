package com.spendwise.service.impl;

import com.spendwise.dto.TransactionRequest;
import com.spendwise.dto.TransactionResponse;
import com.spendwise.entity.Category;
import com.spendwise.entity.Transaction;
import com.spendwise.entity.TransactionType;
import com.spendwise.entity.User;
import com.spendwise.exception.BadRequestException;
import com.spendwise.exception.ForbiddenException;
import com.spendwise.exception.ResourceNotFoundException;
import com.spendwise.mapper.TransactionMapper;
import com.spendwise.repository.CategoryRepository;
import com.spendwise.repository.TransactionRepository;
import com.spendwise.service.CurrentUserProvider;
import com.spendwise.service.TransactionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionMapper mapper;
    private final CurrentUserProvider currentUserProvider;

    public TransactionServiceImpl(TransactionRepository transactionRepository,
                                 CategoryRepository categoryRepository,
                                 TransactionMapper mapper,
                                 CurrentUserProvider currentUserProvider) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.mapper = mapper;
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getAll(Pageable pageable, TransactionType type, Long categoryId, LocalDate startDate, LocalDate endDate, String title) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BadRequestException("Start date cannot be after end date");
        }
        Specification<Transaction> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            User currentUser = currentUserProvider.getCurrentUser();
            predicates.add(cb.equal(root.get("user").get("userId"), currentUser.getUserId()));
            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("categoryId"), categoryId));
            }
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("transactionDate"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("transactionDate"), endDate));
            }
            if (title != null && !title.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase().trim() + "%"));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return transactionRepository.findAll(spec, pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        User currentUser = currentUserProvider.getCurrentUser();
        if (!transaction.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new ForbiddenException("Cannot access this transaction");
        }
        return mapper.toResponse(transaction);
    }

    @Override
    @Transactional
    public TransactionResponse create(TransactionRequest request) {
        User currentUser = currentUserProvider.getCurrentUser();
        Transaction transaction = mapper.toEntity(request);
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            if (category.getType() != request.getType()) {
                throw new BadRequestException("Category type does not match transaction type");
            }
            transaction.setCategory(category);
        }
        transaction.setUser(currentUser);
        return mapper.toResponse(transactionRepository.save(transaction));
    }

    @Override
    @Transactional
    public TransactionResponse update(Long id, TransactionRequest request) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        User currentUser = currentUserProvider.getCurrentUser();
        if (!transaction.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new ForbiddenException("Cannot modify this transaction");
        }
        mapper.updateEntity(request, transaction);
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            if (category.getType() != request.getType()) {
                throw new BadRequestException("Category type does not match transaction type");
            }
            transaction.setCategory(category);
        } else {
            transaction.setCategory(null);
        }
        return mapper.toResponse(transactionRepository.save(transaction));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        User currentUser = currentUserProvider.getCurrentUser();
        if (!transaction.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new ForbiddenException("Cannot delete this transaction");
        }
        transactionRepository.delete(transaction);
    }
}
