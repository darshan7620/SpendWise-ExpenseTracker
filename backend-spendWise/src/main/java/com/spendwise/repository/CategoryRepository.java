package com.spendwise.repository;

import com.spendwise.entity.Category;
import com.spendwise.entity.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByOwnerIsNull();
    List<Category> findByOwnerUserId(Long userId);
    Page<Category> findByOwnerIsNullOrOwnerUserId(Long userId, Pageable pageable);
    List<Category> findByOwnerUserIdAndType(Long userId, TransactionType type);
    Optional<Category> findByCategoryNameAndOwnerUserIdAndType(String categoryName, Long userId, TransactionType type);
    Optional<Category> findByCategoryNameAndOwnerIsNullAndType(String categoryName, TransactionType type);
}
