package com.pjx.pjxserver.controller;

import com.pjx.pjxserver.common.JwtUtil;
import com.pjx.pjxserver.domain.Spending;
import com.pjx.pjxserver.service.ReactionService;
import com.pjx.pjxserver.service.SpendingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reaction")
@RequiredArgsConstructor
@Tag(name = "리액션", description = "리액션 관련 API")
public class ReactionController {

    @Autowired
    private final SpendingService spendingService; // 기존 서비스 사용
    @Autowired
    private final ReactionService reactionService; // Reaction 전용 서비스 추가

    @Autowired
    private final JwtUtil jwtUtil;

    // JWT에서 kakaoId를 추출하는 공통 메서드
    private Long extractKakaoIdFromJwt(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7); // "Bearer " 이후의 토큰만 추출
        return Long.valueOf(jwtUtil.extractSubject(token)); // JWT의 subject에서 kakaoId 추출
    }

    @Operation(summary = "특정 날짜의 기분 저장", description = "사용자가 특정 날짜와 기분(리액션 타입)을 저장합니다.")
    @PostMapping("/reaction")
    public ResponseEntity<Map<String, Object>> saveReaction(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam
            @Parameter(description = "저장할 날짜 (YYYY-MM-DD 형식)", example = "2024-12-01")
            String date,
            @RequestParam
            @Parameter(description = "리액션 타입 (HAPPY, WONDER, SURPRISED, SAD, ANGRY 중 하나)", example = "HAPPY")
            String reactionType) {

        Long kakaoId = extractKakaoIdFromJwt(authHeader);
        LocalDate specificDate = LocalDate.parse(date);

        // 리액션 데이터 저장 (ReactionService 사용)
        reactionService.submitReaction(kakaoId, specificDate, reactionType);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "리액션이 성공적으로 저장되었습니다.");
        response.put("date", specificDate);
        response.put("reactionType", reactionType);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "특정 월의 리액션 타입 리스트 조회 (POST)", description = "입력받은 월의 모든 리액션 데이터를 조회합니다.")
    @PostMapping("/reactions/by-month")
    public ResponseEntity<Map<String, Object>> getReactionsByMonthPost(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam
            @Parameter(description = "조회할 월 (YYYY-MM 형식)", example = "2024-09")
            String month) {

        Long kakaoId = extractKakaoIdFromJwt(authHeader);

        // 월 시작과 종료 날짜 계산
        LocalDate startDate = LocalDate.parse(month + "-01");
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        // ReactionService에서 리액션 데이터 조회
        List<Map<String, Object>> reactionList = reactionService.getReactionsByDateRange(kakaoId, startDate, endDate);

        Map<String, Object> response = new HashMap<>();
        response.put("month", month);
        response.put("reactionList", reactionList);

        return ResponseEntity.ok(response);
    }

}
