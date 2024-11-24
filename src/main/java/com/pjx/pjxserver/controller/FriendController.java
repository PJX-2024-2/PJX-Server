package com.pjx.pjxserver.controller;


import com.pjx.pjxserver.common.JwtUtil;
import com.pjx.pjxserver.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
@Tag(name = "친구", description = "친구 관련 API")
public class FriendController {
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private final UserService userService;

    // JWT에서 kakaoId를 추출하는 공통 메서드
    private Long extractKakaoIdFromJwt(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7); // "Bearer " 이후의 토큰만 추출
        return Long.valueOf(jwtUtil.extractSubject(token)); // JWT의 subject에서 kakaoId 추출
    }

    @Operation(
            summary = "친구 팔로우 추가",
            description = "친구 닉네임을 통해 팔로우를 추가합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "팔로우 성공",
                            content = @Content(mediaType = "application/json",
                                    examples = @ExampleObject(value ="""
                            {
                                "message": "팔로우 성공"
                            }
                            """))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청",
                            content = @Content(mediaType = "application/json", examples = @ExampleObject(value ="""
                            {
                                "message": "해당 UserNickname의 친구를 찾을 수 없습니다."
                            }
                            """)))
            }
    )
    @PostMapping("/{friendUserNickname}/follow")
    public ResponseEntity<Map<String, String>> followUser(
            @RequestHeader("Authorization")
            String authHeader,

            @PathVariable
            @Parameter(description = "팔로우할 친구의 닉네임", example = "미키마우스")
            String friendUserNickname) {

        Long userKakaoId = extractKakaoIdFromJwt(authHeader);
        Map<String, String> response = new HashMap<>();
        try {
            String message = userService.followUserByUserNickname(userKakaoId, friendUserNickname);
            response.put("message", message);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }


    @Operation(
            summary = "친구 팔로우 취소",
            description = "친구 닉네임을 통해 팔로우를 취소합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "팔로우 취소 성공",
                            content = @Content(mediaType = "application/json",
                                    examples = @ExampleObject(value = """
                            {
                                "message": "언팔로우 성공"
                            }
                            """))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청",
                            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                            {
                                "message": "해당 UserNickname의 친구를 찾을 수 없습니다."
                            }
                            """)))
            }
    )
    @DeleteMapping("/{friendUserNickname}/follow")
    public ResponseEntity<Map<String, String>> unfollowUser(
            @RequestHeader("Authorization")
            String authHeader,

            @PathVariable
            @Parameter(description = "언팔로우할 친구의 닉네임", example = "미니마우스")
            String friendUserNickname) {

        Long userKakaoId = extractKakaoIdFromJwt(authHeader);
        Map<String, String> response = new HashMap<>();
        try {
            String message = userService.unfollowUserByUserNickname(userKakaoId, friendUserNickname);
            response.put("message", message);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @Operation(
            summary = "팔로우 여부 확인",
            description = "특정 사용자를 팔로우하고 있는지 여부를 확인합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "팔로우 여부 확인 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = {
                                            @ExampleObject(
                                                    name = "팔로우 일 때",
                                                    value = """
                        {
                            "isFollowing": true
                        }
                        """
                                            ),
                                            @ExampleObject(
                                                    name = "팔로우 아닐 때",
                                                    value = """
                        {
                            "isFollowing": false
                        }
                        """
                                            )
                                    }
                            )
                    )
            }
    )

    @GetMapping("/{friendUserNickname}/is-following")
    public ResponseEntity<Map<String, Boolean>> isFollowing(
            @RequestHeader("Authorization")
            String authHeader,

            @PathVariable
            @Parameter(description = "팔로우 여부를 확인할 친구의 닉네임", example = "도날드덕")
            String friendUserNickname) {

        Long userKakaoId = extractKakaoIdFromJwt(authHeader);
        boolean isFollowing = userService.isFollowingByUserNickname(userKakaoId, friendUserNickname);

        Map<String, Boolean> response = new HashMap<>();
        response.put("isFollowing", isFollowing);
        return ResponseEntity.ok(response);
    }

}
