package com.pjx.pjxserver.service;

import com.pjx.pjxserver.domain.Expense;
import com.pjx.pjxserver.domain.SpendingGoal;
import com.pjx.pjxserver.domain.User;
import com.pjx.pjxserver.repository.ExpenseRepository;
import com.pjx.pjxserver.repository.SpendingGoalRepository;
import com.pjx.pjxserver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class SpendingGoalService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SpendingGoalRepository spendingGoalRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    public SpendingGoal setMonthlyGoal(Long kakaoId, BigDecimal goal) {
        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        LocalDate currentMonth = LocalDate.now().withDayOfMonth(1);

        SpendingGoal spendingGoal = spendingGoalRepository.findByUserAndGoalDate(user, currentMonth)
                .orElse(SpendingGoal.builder()
                        .user(user)
                        .goalDate(currentMonth)
                        .monthlyGoal(goal)
                        .currentSpending(BigDecimal.ZERO)
                        .build());

        spendingGoal.setmonthlyGoal(goal);
        return spendingGoalRepository.save(spendingGoal);
    }

    public BigDecimal getCurrentSpending(Long kakaoId, LocalDate month) {
        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        LocalDate start = month.withDayOfMonth(1);
        LocalDate end = month.withDayOfMonth(month.lengthOfMonth());
        List<Expense> expenses = expenseRepository.findByUserAndDateBetween(user, start, end);
        return expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Expense addExpense(Long kakaoId, LocalDate date, BigDecimal amount) {
        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Expense expense = Expense.builder()
                .user(user)
                .date(date)
                .amount(amount)
                .build();

        SpendingGoal spendingGoal = spendingGoalRepository.findByUserAndGoalDate(user, date.withDayOfMonth(1))
                .orElseThrow(() -> new RuntimeException("Spending goal not set for this month"));

        spendingGoal.setCurrentSpending(spendingGoal.getCurrentSpending().add(amount));
        spendingGoalRepository.save(spendingGoal);

        return expenseRepository.save(expense);
    }

    public BigDecimal getTodaySpending(Long kakaoId) {
        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDate today = LocalDate.now();

        // Fetch expenses for today
        List<Expense> expenses = expenseRepository.findByUserAndDate(user, today);

        // Calculate the total amount spent today
        return expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // 특정 날짜의 지출 항목 추가
    public BigDecimal getSpendingByDate(Long kakaoId, LocalDate date) {
        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 특정 날짜의 지출 항목들을 조회
        List<Expense> expenses = expenseRepository.findByUserAndDate(user, date);

        // 해당 날짜의 총 지출 금액 계산
        return expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    // 이번 달 목표 지출을 조회하는 메서드 추가
    public BigDecimal getMonthlyGoal(Long kakaoId) {
        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        LocalDate currentMonth = LocalDate.now().withDayOfMonth(1);

        // 현재 달의 SpendingGoal을 찾고 목표 금액을 반환 (없을 경우 0 반환)
        return spendingGoalRepository.findByUserAndGoalDate(user, currentMonth)
                .map(SpendingGoal::getMonthlyGoal)
                .orElse(BigDecimal.ZERO);
    }

    // 이번 달 목표 지출을 수정하는 메서드 추가
    public SpendingGoal updateMonthlyGoal(Long kakaoId, BigDecimal newGoal) {
        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        LocalDate currentMonth = LocalDate.now().withDayOfMonth(1);

        // 현재 달의 SpendingGoal을 조회 후 목표 금액 업데이트
        SpendingGoal spendingGoal = spendingGoalRepository.findByUserAndGoalDate(user, currentMonth)
                .orElse(SpendingGoal.builder()
                        .user(user)
                        .goalDate(currentMonth)
                        .monthlyGoal(BigDecimal.ZERO)
                        .currentSpending(BigDecimal.ZERO)
                        .build());

        spendingGoal.setMonthlyGoal(newGoal);
        return spendingGoalRepository.save(spendingGoal);
    }
}