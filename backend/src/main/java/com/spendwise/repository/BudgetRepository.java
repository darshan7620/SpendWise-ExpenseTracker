package com.spendwise.repository;

import com.spendwise.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
    List<Budget> findByUserUserId(Long userId);
    List<Budget> findByUserUserIdAndMonthAndYear(Long userId, Integer month, Integer year);
    Optional<Budget> findByUserUserIdAndCategoryCategoryIdAndMonthAndYear(Long userId, Long categoryId, Integer month, Integer year);
    boolean existsByUserUserIdAndCategoryCategoryIdAndMonthAndYear(Long userId, Long categoryId, Integer month, Integer year);
}
