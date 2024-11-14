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
    @Operation(summary = "홈 - 유저가 한달 목표를 설정하는 PUT method api")
    @PutMapping("/goal")
    public ResponseEntity<SpendingGoal> setMonthlyGoal(@RequestParam Long kakaoId, @RequestParam BigDecimal goal) {
        return ResponseEntity.ok(spendingService.setMonthlyGoal(kakaoId, goal));
    }

    @Operation(summary = "홈1 - 유저가 설정한 한달 목표 지출을 받을 수 있는 GET method api")
    @GetMapping("/goal")
    public ResponseEntity<Map<String, Object>> getMonthlyGoal(@RequestParam Long kakaoId) {
        BigDecimal monthlyGoal = spendingService.getMonthlyGoal(kakaoId);

        Map<String, Object> response = new HashMap<>();
        response.put("goal", monthlyGoal);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "홈2 - 유저가 수정한 한달 목표 지출을 보낼 수 있는 POST method api")
    @PostMapping("/goal")
    public ResponseEntity<SpendingGoal> updateMonthlyGoal(@RequestParam Long kakaoId, @RequestParam BigDecimal newGoal) {
        SpendingGoal updatedGoal = spendingService.updateMonthlyGoal(kakaoId, newGoal);
        return ResponseEntity.ok(updatedGoal);
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