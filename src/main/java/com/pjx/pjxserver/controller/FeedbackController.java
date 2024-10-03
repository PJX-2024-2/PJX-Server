package com.pjx.pjxserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "FeedbackController", description = "FeedbackController 관련 API")
public class FeedbackController {

    @Operation(summary = "피드백 작성")
    @PostMapping("/feedback")
    public String feedback() {
        return "피드백 작성";
    }

    @Operation(summary = "공감(좋아요) 추가")
    @PostMapping("/likes")
    public String likes() {
        return "공감(좋아요) 추가";
    }
}