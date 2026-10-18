package com.spendwise.mapper;

import com.spendwise.dto.TransactionRequest;
import com.spendwise.dto.TransactionResponse;
import com.spendwise.entity.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {
    public Transaction toEntity(TransactionRequest request) {
        Transaction transaction = new Transaction();
        transaction.setTitle(request.getTitle());
        transaction.setAmount(request.getAmount());
        transaction.setType(request.getType());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setNotes(request.getNotes());
        transaction.setPaymentMethod(request.getPaymentMethod());
        return transaction;
    }

    public void updateEntity(TransactionRequest request, Transaction transaction) {
        transaction.setTitle(request.getTitle());
        transaction.setAmount(request.getAmount());
        transaction.setType(request.getType());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setNotes(request.getNotes());
        transaction.setPaymentMethod(request.getPaymentMethod());
    }

    public TransactionResponse toResponse(Transaction transaction) {
        TransactionResponse response = new TransactionResponse();
        response.setTransactionId(transaction.getTransactionId());
        response.setTitle(transaction.getTitle());
        response.setAmount(transaction.getAmount());
        response.setType(transaction.getType());
        response.setTransactionDate(transaction.getTransactionDate());
        response.setNotes(transaction.getNotes());
        response.setPaymentMethod(transaction.getPaymentMethod());
        response.setUserId(transaction.getUser() != null ? transaction.getUser().getUserId() : null);
        response.setCategoryId(transaction.getCategory() != null ? transaction.getCategory().getCategoryId() : null);
        response.setCreatedAt(transaction.getCreatedAt());
        return response;
    }
}
