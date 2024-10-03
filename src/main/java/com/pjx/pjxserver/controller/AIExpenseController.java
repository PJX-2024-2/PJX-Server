package com.pjx.pjxserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "AIExpenseController", description = "AIExpenseController API")
public class AIExpenseController {

    @Operation(summary = "AI를 통한 지출 자동 기록")
    @PostMapping("ai/expenses")
    public String expenses() {
        return "AI를 통한 지출 자동 기록";
    }
}
