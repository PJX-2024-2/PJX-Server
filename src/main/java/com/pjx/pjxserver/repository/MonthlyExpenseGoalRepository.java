package com.pjx.pjxserver.repository;

import com.pjx.pjxserver.domain.MonthlyExpenseGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MonthlyExpenseGoalRepository extends JpaRepository<MonthlyExpenseGoal, Long> {
    List<MonthlyExpenseGoal> findByUserIdAndYearAndMonth(Long userId, Integer year, Integer month);
}