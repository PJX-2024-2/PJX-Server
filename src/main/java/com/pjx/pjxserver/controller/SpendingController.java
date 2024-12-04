package com.pjx.pjxserver.controller;

import com.pjx.pjxserver.common.JwtUtil;
import com.pjx.pjxserver.domain.Spending;
import com.pjx.pjxserver.service.SpendingGoalService;
import com.pjx.pjxserver.service.SpendingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/spending")
@RequiredArgsConstructor
@Tag(name = "지출", description = "지출 관련 API")
public class SpendingController {

    @Autowired
    private SpendingService spendingService;

    @Autowired
    private SpendingGoalService spendingGoalService;

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

    @Operation(summary = "직접/AI 지출 등록 방식 중 선택", description = "사용자가 등록 방식을 선택합니다.")
    @PostMapping("/select-method")
    public ResponseEntity<Map<String, String>> selectSpendingMethod(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam
            @Parameter(description = "사용자가 선택한 등록 방식", example = "AI_RECEIPT 또는 MANUAL")
            String method) {

        Long kakaoId = extractKakaoIdFromJwt(authHeader);

        Map<String, String> response = new HashMap<>();
        if ("AI_RECEIPT".equals(method)) {
            response.put("nextStep", "/api/spending/add/receipt");
            response.put("message", "AI 영수증 분석을 위해 영수증을 업로드하세요.");
        } else if ("MANUAL".equals(method)) {
            response.put("nextStep", "/api/spending/manual/create");
            response.put("message", "지출 항목을 직접 작성하세요.");
        } else {
            response.put("message", "잘못된 선택입니다. 올바른 방식을 선택하세요.");
        }

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "직접 - 지출 항목 생성", description = "사용자가 직접 지출 항목을 생성합니다.")
    @PostMapping(value = "/create", consumes = "multipart/form-data")
    public ResponseEntity<Map<String, Object>> createSpending(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam
            @Parameter(description = "지출 날짜 (YYYY-MM-DD 형식)", example = "2024-11-24") String date,
            @RequestParam
            @Parameter(description = "지출 금액", example = "50000") BigDecimal amount,
            @RequestParam
            @Parameter(description = "지출 설명", example = "점심 식사") String description,
            @RequestParam(required = false)
            @Parameter(description = "지출 추가 정보", example = "친구와 점심") String note,
            @RequestParam(required = false)
            @Parameter(description = "지출 관련 이미지") List<MultipartFile> images) {

        Long kakaoId = extractKakaoIdFromJwt(authHeader);

        LocalDate spendingDate = LocalDate.parse(date);

        try {
            Spending spending = spendingService.createSpending(kakaoId, spendingDate, amount, description, note, images);

            Map<String, Object> response = new HashMap<>();
            response.put("spendingId", spending.getId());
            response.put("message", "지출 항목이 성공적으로 저장되었습니다.");
            response.put("data", spending);

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("message", "이미지 업로드에 실패했습니다."));
        }
    }

    @Operation(summary = "직접 - 지출 항목 수정", description = "사용자가 직접 지출 항목의 세부 내용을 수정합니다.")
    @PutMapping("/update")
    public ResponseEntity<Map<String, Object>> updateSpending(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam Long spendingId,
            @RequestParam(required = false) BigDecimal amount,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String note,
            @RequestParam(required = false) List<String> images) {

        Long kakaoId = extractKakaoIdFromJwt(authHeader);
        Spending updatedSpending = spendingService.updateSpending(spendingId, amount, description, note, images);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "지출 항목이 성공적으로 수정되었습니다.");
        response.put("data", updatedSpending);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "직접 - 지출 항목 삭제", description = "사용자가 특정 지출 항목을 삭제합니다.")
    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, String>> deleteSpending(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam
            @Parameter(description = "삭제할 지출 항목의 ID", example = "1")
            Long spendingId) {

        Long kakaoId = extractKakaoIdFromJwt(authHeader);
        spendingService.deleteSpending(spendingId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "지출 항목이 성공적으로 삭제되었습니다.");

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "직접 - 지출 항목 세부 정보 조회", description = "특정 지출 항목의 세부 정보를 조회합니다.")
    @GetMapping("/detail")
    public ResponseEntity<Map<String, Object>> getSpendingDetail(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam
            @Parameter(description = "지출 항목 ID", example = "1")
            Long spendingId) {

        Long kakaoId = extractKakaoIdFromJwt(authHeader);
        Spending spending = spendingService.getSpendingDetail(spendingId)
                .orElseThrow(() -> new RuntimeException("Spending not found"));

        Map<String, Object> response = new HashMap<>();
        response.put("data", spending);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "홈3 - 해당하는 날을 눌렀을때 날짜를 보내주면 그날에 대한 지출 목록 리스트를 보내주는 POST method api", description = "특정 날짜의 지출 내역을 리스트 형식으로 조회합니다.")
    @PostMapping("/list")
    public ResponseEntity<Map<String, Object>> getSpendingListByDate(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam
            @Parameter(description = "조회할 날짜 (YYYY-MM-DD 형식)", example = "2024-11-24")
            String date) {

        Long kakaoId = extractKakaoIdFromJwt(authHeader);
        LocalDate spendingDate = LocalDate.parse(date);

        List<Spending> spendingList = spendingService.getSpendingListByDate(kakaoId, spendingDate);

        List<Map<String, Object>> spendingData = spendingList.stream()
                .map(spending -> {
                    Map<String, Object> spendingInfo = new HashMap<>();
                    spendingInfo.put("description", spending.getDescription());
                    spendingInfo.put("amount", spending.getAmount());
                    spendingInfo.put("images", spending.getImages());
                    spendingInfo.put("note", spending.getNote());
                    return spendingInfo;
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("date", date);
        response.put("spendingList", spendingData);

        return ResponseEntity.ok(response);
    }


//    // 이번달 총 지출을 조회하는 GET 메서드
//    @Operation(summary = "이번달 총 지출 조회")
//    @GetMapping("/current")
//    public ResponseEntity<Map<String, Object>> getCurrentSpending(
//            @RequestHeader("Authorization") String authHeader,
//            @RequestParam
//            @Parameter(description = "조회할 월 (YYYY-MM 형식)", example = "2024-11")
//            String month) {
//
//        Long kakaoId = extractKakaoIdFromJwt(authHeader);
//        LocalDate monthDate = LocalDate.parse(month + "-01");
//        BigDecimal currentSpending = spendingGoalService.getCurrentSpending(kakaoId, monthDate);
//
//        Map<String, Object> response = new HashMap<>();
//        response.put("currentSpending", currentSpending);
//        response.put("month", month);
//
//        return ResponseEntity.ok(response);
//    }
@Operation(summary = "이번달 총 지출 조회")
@PostMapping("/current")
public ResponseEntity<Map<String, Object>> getCurrentSpendingPost(
        @RequestHeader("Authorization") String authHeader,
        @RequestParam
        @Parameter(description = "조회할 월 (YYYY-MM 형식)", example = "2024-11")
        String month) {

    if (month == null || month.isEmpty()) {
        throw new IllegalArgumentException("Month must be provided as a query parameter.");
    }

    Long kakaoId = extractKakaoIdFromJwt(authHeader);
    LocalDate monthDate = LocalDate.parse(month + "-01"); // "YYYY-MM" 형식 처리
    BigDecimal currentSpending = spendingGoalService.getCurrentSpending(kakaoId, monthDate);

    Map<String, Object> response = new HashMap<>();
    response.put("currentSpending", currentSpending);
    response.put("month", month);

    return ResponseEntity.ok(response);
}


//    // 특정 날짜의 지출 항목을 추가하는 POST 메서드
//    @Operation(summary = "특정 날짜의 지출 항목 추가")
//    @PostMapping("/expense")
//    public ResponseEntity<Expense> addExpense(
//            @RequestHeader("Authorization") String authHeader,
//            @RequestParam String date,
//            @RequestParam BigDecimal amount) {
//
//        Long kakaoId = extractKakaoIdFromJwt(authHeader);
//        LocalDate expenseDate = LocalDate.parse(date);
//        Expense newExpense = spendingService.addExpense(kakaoId, expenseDate, amount);
//
//        return ResponseEntity.ok(newExpense);
//    }

    // 오늘의 지출을 조회하는 GET 메서드
    @Operation(summary = "오늘 지출 조회")
    @GetMapping("/today")
    public ResponseEntity<Map<String, Object>> getTodaySpending(@RequestHeader("Authorization") String authHeader) {
        Long kakaoId = extractKakaoIdFromJwt(authHeader);
        BigDecimal todaySpending = spendingGoalService.getTodaySpending(kakaoId);

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
            @RequestParam
            @Parameter(description = "조회할 날짜 (YYYY-MM-DD 형식)", example = "2024-11-24")
            String date) {

        Long kakaoId = extractKakaoIdFromJwt(authHeader);
        LocalDate specificDate = LocalDate.parse(date);
        BigDecimal spendingByDate = spendingGoalService.getSpendingByDate(kakaoId, specificDate);

        Map<String, Object> response = new HashMap<>();
        response.put("spendingByDate", spendingByDate);
        response.put("date", specificDate);

        return ResponseEntity.ok(response);
    }
}

