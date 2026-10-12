package com.spendwise.service;

import com.spendwise.dto.TransactionRequest;
import com.spendwise.dto.TransactionResponse;
import com.spendwise.entity.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface TransactionService {
    Page<TransactionResponse> getAll(Pageable pageable, TransactionType type, Long categoryId, LocalDate startDate, LocalDate endDate, String title);
    TransactionResponse getById(Long id);
    TransactionResponse create(TransactionRequest request);
    TransactionResponse update(Long id, TransactionRequest request);
    void delete(Long id);
}
