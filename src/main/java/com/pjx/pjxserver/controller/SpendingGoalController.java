package com.pjx.pjxserver.controller;

import com.pjx.pjxserver.common.JwtUtil;
import com.pjx.pjxserver.domain.Expense;
import com.pjx.pjxserver.domain.SpendingGoal;
import com.pjx.pjxserver.service.SpendingGoalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
@RestController
@RequestMapping("/api/spending")
@RequiredArgsConstructor
@Tag(name = "지출 목표", description = "지출 목표 관련 API")
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
    @Operation(summary = "홈 - 한 달 목표 설정", description = "유저가 한달 목표를 설정하는 PUT method api",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "목표 설정 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                      "id": 3,
                                                      "user": {
                                                        "id": 7,
                                                        "kakaoId": 375402,
                                                        "nickname": "포차코",
                                                        "userNickname": "코코",
                                                        "profileImageUrl": "프로필 url"
                                                      },
                                                      "monthlyGoal": 50000,
                                                      "currentSpending": 0,
                                                      "goalDate": "2024-11-01"
                                                    }
                                    """
                                    )
                            )
                    )
            }
    )
    @PutMapping("/goal")
    public ResponseEntity<SpendingGoal> setMonthlyGoal(
            @RequestHeader("Authorization")
            String authHeader,
            @RequestParam
            @Parameter(description = "사용자가 설정할 한 달 목표 금액", example = "500000")
            BigDecimal goal) {

        Long kakaoId = extractKakaoIdFromJwt(authHeader);
        SpendingGoal monthlyGoal = spendingService.setMonthlyGoal(kakaoId, goal);
        return ResponseEntity.ok(monthlyGoal);
    }


    // 유저가 설정한 한달 목표 지출을 가져오는 GET 메서드
    @Operation(summary = "홈1 - 한 달 목표 조회", description = "유저가 설정한 한달 목표 지출을 받을 수 있는 GET method api",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "목표 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = """
                                    {
                                        "goal": 500000
                                    }
                                    """
                                    )
                            )
                    )
            })
    @GetMapping("/goal")
    public ResponseEntity<Map<String, Object>> getMonthlyGoal(
            @RequestHeader("Authorization")
            String authHeader) {
        Long kakaoId = extractKakaoIdFromJwt(authHeader);
        BigDecimal monthlyGoal = spendingService.getMonthlyGoal(kakaoId);

        Map<String, Object> response = new HashMap<>();
        response.put("goal", monthlyGoal);

        return ResponseEntity.ok(response);
    }

    // 유저가 한달 목표 지출을 수정하는 POST 메서드
    @Operation(summary = "홈2 - 한 달 목표 수정", description = "유저가 수정한 한달 목표 지출을 보낼 수 있는 POST method api",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "목표 수정 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = """
                                    {
                                                      "id": 3,
                                                      "user": {
                                                        "id": 7,
                                                        "kakaoId": 375402,
                                                        "nickname": "포차코",
                                                        "userNickname": "코코",
                                                        "profileImageUrl": "프로필 url"
                                                      },
                                                      "monthlyGoal": 80000,
                                                      "currentSpending": 0,
                                                      "goalDate": "2024-11-01"
                                                    }
                                    """
                                    )
                            )
                    )
            })
    @PostMapping("/goal")
    public ResponseEntity<SpendingGoal> updateMonthlyGoal(
            @RequestHeader("Authorization")
            String authHeader,

            @RequestParam
            @Parameter(description = "사용자가 수정할 한 달 목표 금액", example = "600000")
            BigDecimal newGoal) {

        Long kakaoId = extractKakaoIdFromJwt(authHeader);
        SpendingGoal updatedGoal = spendingService.updateMonthlyGoal(kakaoId, newGoal);
        return ResponseEntity.ok(updatedGoal);
    }

}
