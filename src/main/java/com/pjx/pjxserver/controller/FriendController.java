package com.pjx.pjxserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Tag(name = "FriendController", description = "FriendController API")
public class FriendController {

    @Operation(summary = "친구 추가")
    @PostMapping("/friends")
    public String friends() {
        return "친구추가";
    }

    @Operation(summary = "친구 목록 조회")
    @GetMapping("/friends/{userId}")
    public String userId() {
        return "친구 목록 조회";
    }

    @Operation(summary = "친구 삭제")
    @DeleteMapping("/friends/{userId}/{friendId}")
    public String friendId() {
        return "친구 삭제";
    }
}