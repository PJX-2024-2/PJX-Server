package com.pjx.pjxserver.controller;

import com.pjx.pjxserver.domain.Spending;
import com.pjx.pjxserver.service.SpendingService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/spending/manual")
public class SpendingController {

    @Autowired
    private SpendingService spendingService;

    @Operation(summary = "직접/AI 지출 등록 방식 중 선택", description = "사용자가 등록 방식을 선택합니다.")
    @PostMapping("/select-method")
    public ResponseEntity<Map<String, String>> selectSpendingMethod(
            @RequestParam Long kakaoId,
            @RequestParam String method) {

        Map<String, String> response = new HashMap<>();
        if ("AI_RECEIPT".equals(method)) {
            response.put("nextStep", "/api/spending/add/receipt"); // AI 영수증 API로 연결
            response.put("message", "AI 영수증 분석을 위해 영수증을 업로드하세요.");
        } else if ("MANUAL".equals(method)) {
            response.put("nextStep", "/api/spending/manual/create"); // 직접 작성 API로 연결
            response.put("message", "지출 항목을 직접 작성하세요.");
        } else {
            response.put("message", "잘못된 선택입니다. 올바른 방식을 선택하세요.");
        }

        return ResponseEntity.ok(response);
    }

    // 지출 항목 생성 (특정 날짜 지정 가능)
    @Operation(summary = "직접 - 지출 항목 생성", description = "사용자가 직접 지출 항목을 생성합니다.")
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createSpending(
            @RequestParam Long kakaoId,
            @RequestParam String date, // 특정 날짜 추가
            @RequestParam BigDecimal amount,
            @RequestParam String description,
            @RequestParam(required = false) String note,
            @RequestParam(required = false) List<String> images) {

        LocalDate spendingDate = LocalDate.parse(date); // 문자열 날짜를 LocalDate로 변환
        Spending spending = spendingService.createSpending(kakaoId, spendingDate, amount, description, note, images);

        Map<String, Object> response = new HashMap<>();
        response.put("spendingId", spending.getId());
        response.put("message", "지출 항목이 성공적으로 저장되었습니다.");
        response.put("data", spending);

        return ResponseEntity.ok(response);
    }

    // 지출 항목 수정
    @Operation(summary = "직접 - 지출 항목 수정", description = "사용자가 직접 지출 항목의 세부 내용을 수정합니다.")
    @PutMapping("/update")
    public ResponseEntity<Map<String, Object>> updateSpending(
            @RequestParam Long spendingId,
            @RequestParam(required = false) BigDecimal amount,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String note,
            @RequestParam(required = false) List<String> images) {

        Spending updatedSpending = spendingService.updateSpending(spendingId, amount, description, note, images);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "지출 항목이 성공적으로 수정되었습니다.");
        response.put("data", updatedSpending);

        return ResponseEntity.ok(response);
    }


    // 지출 항목 삭제
    @Operation(summary = "직접 - 지출 항목 삭제", description = "사용자가 특정 지출 항목을 삭제합니다.")
    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, String>> deleteSpending(@RequestParam Long spendingId) {
        spendingService.deleteSpending(spendingId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "지출 항목이 성공적으로 삭제되었습니다.");

        return ResponseEntity.ok(response);
    }

    // 지출 항목 세부 정보 조회
    @Operation(summary = "직접 - 지출 항목 세부 정보 조회", description = "특정 지출 항목의 세부 정보를 조회합니다.")
    @GetMapping("/detail")
    public ResponseEntity<Map<String, Object>> getSpendingDetail(@RequestParam Long spendingId) {
        Spending spending = spendingService.getSpendingDetail(spendingId)
                .orElseThrow(() -> new RuntimeException("Spending not found"));

        Map<String, Object> response = new HashMap<>();
        response.put("data", spending);

        return ResponseEntity.ok(response);
    }
}
