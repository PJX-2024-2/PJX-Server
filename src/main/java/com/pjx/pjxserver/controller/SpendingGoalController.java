package com.pjx.pjxserver.controller;

import com.pjx.pjxserver.common.JwtUtil;
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

    @Autowired
    private JwtUtil jwtUtil;

    // JWT에서 kakaoId를 추출하는 공통 메서드
    private Long extractKakaoIdFromJwt(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7); // "Bearer " 이후의 토큰만 추출
        return Long.valueOf(jwtUtil.extractSubject(token)); // JWT의 subject에서 kakaoId 추출
    }

    // 유저가 한달 목표를 설정하는 PUT 메서드
    @Operation(summary = "홈 - 유저가 한달 목표를 설정하는 PUT method api")
    @PutMapping("/goal")
    public ResponseEntity<SpendingGoal> setMonthlyGoal(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam BigDecimal goal) {

        Long kakaoId = extractKakaoIdFromJwt(authHeader);
        SpendingGoal monthlyGoal = spendingService.setMonthlyGoal(kakaoId, goal);
        return ResponseEntity.ok(monthlyGoal);
    }

    // 유저가 설정한 한달 목표 지출을 가져오는 GET 메서드
    @Operation(summary = "홈1 - 유저가 설정한 한달 목표 지출을 받을 수 있는 GET method api")
    @GetMapping("/goal")
    public ResponseEntity<Map<String, Object>> getMonthlyGoal(@RequestHeader("Authorization") String authHeader) {
        Long kakaoId = extractKakaoIdFromJwt(authHeader);
        BigDecimal monthlyGoal = spendingService.getMonthlyGoal(kakaoId);

        Map<String, Object> response = new HashMap<>();
        response.put("goal", monthlyGoal);

        return ResponseEntity.ok(response);
    }

    // 유저가 한달 목표 지출을 수정하는 POST 메서드
    @Operation(summary = "홈2 - 유저가 수정한 한달 목표 지출을 보낼 수 있는 POST method api")
    @PostMapping("/goal")
    public ResponseEntity<SpendingGoal> updateMonthlyGoal(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam BigDecimal newGoal) {

        Long kakaoId = extractKakaoIdFromJwt(authHeader);
        SpendingGoal updatedGoal = spendingService.updateMonthlyGoal(kakaoId, newGoal);
        return ResponseEntity.ok(updatedGoal);
    }

    // 이번달 총 지출을 조회하는 GET 메서드
    @Operation(summary = "이번달 총 지출 조회")
    @GetMapping("/current")
    public ResponseEntity<Map<String, Object>> getCurrentSpending(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String month) {

        Long kakaoId = extractKakaoIdFromJwt(authHeader);
        LocalDate monthDate = LocalDate.parse(month + "-01");
        BigDecimal currentSpending = spendingService.getCurrentSpending(kakaoId, monthDate);

        Map<String, Object> response = new HashMap<>();
        response.put("currentSpending", currentSpending);
        response.put("month", month);

        return ResponseEntity.ok(response);
    }

    // 특정 날짜의 지출 항목을 추가하는 POST 메서드
    @Operation(summary = "특정 날짜의 지출 항목 추가")
    @PostMapping("/expense")
    public ResponseEntity<Expense> addExpense(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String date,
            @RequestParam BigDecimal amount) {

        Long kakaoId = extractKakaoIdFromJwt(authHeader);
        LocalDate expenseDate = LocalDate.parse(date);
        Expense newExpense = spendingService.addExpense(kakaoId, expenseDate, amount);

        return ResponseEntity.ok(newExpense);
    }

    // 오늘의 지출을 조회하는 GET 메서드
    @Operation(summary = "오늘 지출 조회")
    @GetMapping("/today")
    public ResponseEntity<Map<String, Object>> getTodaySpending(@RequestHeader("Authorization") String authHeader) {
        Long kakaoId = extractKakaoIdFromJwt(authHeader);
        BigDecimal todaySpending = spendingService.getTodaySpending(kakaoId);

        Map<String, Object> response = new HashMap<>();
        response.put("todaySpending", todaySpending);
        response.put("date", LocalDate.now());

        return ResponseEntity.ok(response);
    }

    // 특정 날짜의 지출을 조회하는 GET 메서드
    @Operation(summary = "특정 날짜의 지출 조회 YYYY-MM-DD형식으로")
    @GetMapping("/date")
    public ResponseEntity<Map<String, Object>> getSpendingByDate(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String date) {

        Long kakaoId = extractKakaoIdFromJwt(authHeader);
        LocalDate specificDate = LocalDate.parse(date);
        BigDecimal spendingByDate = spendingService.getSpendingByDate(kakaoId, specificDate);

        Map<String, Object> response = new HashMap<>();
        response.put("spendingByDate", spendingByDate);
        response.put("date", specificDate);

        return ResponseEntity.ok(response);
    }
}
