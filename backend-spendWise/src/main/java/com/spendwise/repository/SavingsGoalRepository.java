package com.spendwise.repository;

import com.spendwise.entity.GoalStatus;
import com.spendwise.entity.SavingsGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SavingsGoalRepository extends JpaRepository<SavingsGoal, Long> {
    List<SavingsGoal> findByUserUserId(Long userId);
    List<SavingsGoal> findByUserUserIdAndStatus(Long userId, GoalStatus status);
}
