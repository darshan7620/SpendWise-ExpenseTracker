package com.spendwise.repository;

import com.spendwise.entity.Transaction;
import com.spendwise.entity.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {
    List<Transaction> findByUserUserId(Long userId);
    List<Transaction> findByUserUserIdAndTransactionDateBetween(Long userId, LocalDate start, LocalDate end);
    List<Transaction> findByUserUserIdAndType(Long userId, TransactionType type);
    List<Transaction> findByUserUserIdAndCategoryCategoryId(Long userId, Long categoryId);
    Page<Transaction> findAll(Specification<Transaction> spec, Pageable pageable);
}
