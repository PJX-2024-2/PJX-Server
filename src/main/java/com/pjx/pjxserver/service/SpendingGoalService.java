package com.pjx.pjxserver.service;

import com.pjx.pjxserver.domain.Expense;
import com.pjx.pjxserver.domain.Spending;
import com.pjx.pjxserver.domain.SpendingGoal;
import com.pjx.pjxserver.domain.User;
import com.pjx.pjxserver.repository.ExpenseRepository;
import com.pjx.pjxserver.repository.SpendingGoalRepository;
import com.pjx.pjxserver.repository.SpendingRepository;
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
    private SpendingRepository spendingRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    // 한 달 목표 설정
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

        spendingGoal.setMonthlyGoal(goal);
        return spendingGoalRepository.save(spendingGoal);
    }

    // 이번 달 총 지출 조회 (Spending + SpendingGoal 데이터 포함)
    public BigDecimal getCurrentSpending(Long kakaoId, LocalDate month) {
        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Spending 테이블에서 해당 월의 지출 총합 계산
        LocalDate start = month.withDayOfMonth(1);
        LocalDate end = month.withDayOfMonth(month.lengthOfMonth());
        BigDecimal spendingSum = spendingRepository.findAllByKakaoIdAndDateBetween(kakaoId, start, end).stream()
                .map(Spending::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // SpendingGoal의 currentSpending 값과 합산
        return spendingGoalRepository.findByUserAndGoalDate(user, month.withDayOfMonth(1))
                .map(goal -> goal.getCurrentSpending().add(spendingSum))
                .orElse(spendingSum); // SpendingGoal이 없는 경우 Spending 데이터만 반환
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

    // 오늘의 지출 조회 (Spending 데이터 기반)
    public BigDecimal getTodaySpending(Long kakaoId) {
        LocalDate today = LocalDate.now();

        return spendingRepository.findByKakaoIdAndDate(kakaoId, today).stream()
                .map(Spending::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // 특정 날짜의 지출 조회 (Spending 데이터 기반)
    public BigDecimal getSpendingByDate(Long kakaoId, LocalDate date) {
        return spendingRepository.findByKakaoIdAndDate(kakaoId, date).stream()
                .map(Spending::getAmount)
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
