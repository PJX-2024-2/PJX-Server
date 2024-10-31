package com.pjx.pjxserver.controller;

import com.pjx.pjxserver.domain.Expense;
import com.pjx.pjxserver.domain.SpendingGoal;
import com.pjx.pjxserver.service.SpendingGoalService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/spending")
public class SpendingGoalController {

    @Autowired
    private SpendingGoalService spendingService;
    @Operation(summary = "이번달 지출 목표 설정")
    @PutMapping("/goal")
    public ResponseEntity<SpendingGoal> setMonthlyGoal(@RequestParam Long kakaoId, @RequestParam BigDecimal goal) {
        return ResponseEntity.ok(spendingService.setMonthlyGoal(kakaoId, goal));
    }

    @Operation(summary = "이번달 총 지출 조회")
    @GetMapping("/current")
    public ResponseEntity<Map<String, Object>> getCurrentSpending(@RequestParam Long kakaoId, @RequestParam String month) {
        LocalDate monthDate = LocalDate.parse(month + "-01");
        BigDecimal currentSpending = spendingService.getCurrentSpending(kakaoId, monthDate);

        Map<String, Object> response = new HashMap<>();
        response.put("amount", currentSpending);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "특정 날짜의 지출 항목 추가")
    @PostMapping("/expense")
    public ResponseEntity<Expense> addExpense(@RequestParam Long kakaoId, @RequestParam String date, @RequestParam BigDecimal amount) {
        LocalDate expenseDate = LocalDate.parse(date);
        return ResponseEntity.ok(spendingService.addExpense(kakaoId, expenseDate, amount));
    }

    @Operation(summary = "오늘 지출 조회")
    @GetMapping("/today")
    public ResponseEntity<Map<String, Object>> getTodaySpending(@RequestParam Long kakaoId) {
        BigDecimal todaySpending = spendingService.getTodaySpending(kakaoId);

        Map<String, Object> response = new HashMap<>();
        response.put("amount", todaySpending);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "특정 날짜의 지출 조회 YYYY-MM-DD형식으로")
    @GetMapping("/date")
    public ResponseEntity<Map<String, Object>> getSpendingByDate(@RequestParam Long kakaoId, @RequestParam String date) {
        LocalDate specificDate = LocalDate.parse(date);
        BigDecimal spendingByDate = spendingService.getSpendingByDate(kakaoId, specificDate);

        // Map에 응답 데이터 감싸기
        Map<String, Object> response = new HashMap<>();
        response.put("amount", spendingByDate);

        return ResponseEntity.ok(response);
    }
}
