package com.pjx.pjxserver.service;


import com.pjx.pjxserver.domain.MonthlyExpenseGoal;
import com.pjx.pjxserver.repository.MonthlyExpenseGoalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MonthlyExpenseGoalService {
    @Autowired
    private MonthlyExpenseGoalRepository goalRepository;

    public List<MonthlyExpenseGoal> getUserGoals(Long userId, Integer year, Integer month) {
        return goalRepository.findByUserIdAndYearAndMonth(userId, year, month);
    }

    public MonthlyExpenseGoal createGoal(MonthlyExpenseGoal goal) {
        goal.setCreatedAt(LocalDateTime.now());
        goal.setUpdatedAt(LocalDateTime.now());
        return goalRepository.save(goal);
    }
}