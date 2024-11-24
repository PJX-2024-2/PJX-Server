package com.pjx.pjxserver.controller;

import com.pjx.pjxserver.common.JwtUtil;
import com.pjx.pjxserver.dto.NicknameCheckResponseDto;
import com.pjx.pjxserver.dto.OnboardingRequestDto;
import com.pjx.pjxserver.dto.UserProfileRequestDto;
import com.pjx.pjxserver.service.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import com.pjx.pjxserver.domain.Spending;
import com.pjx.pjxserver.domain.User;
import com.pjx.pjxserver.service.SpendingService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "사용자", description = "사용자 관련 API")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

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
            summary = "프로필 이미지 조회",
            description = "사용자의 프로필 이미지를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "프로필 이미지 URL 반환",
                            content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"))),
                    @ApiResponse(responseCode = "400", description = "유효하지 않은 요청",
                            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{}")))
            }
    )
    @GetMapping("/profile")
    public ResponseEntity<String> getProfileImage(@RequestHeader("Authorization") String authHeader) {
        Long kakaoId = extractKakaoIdFromJwt(authHeader);

        String profileImageUrl = userService.getProfileImageUrl(kakaoId);
        if (profileImageUrl == null) {
            // 프로필 이미지가 없는 경우
            return ResponseEntity.ok("프로필 이미지가 없습니다.");
        }
        return ResponseEntity.ok(profileImageUrl);
    }

    @Operation(
            summary = "프로필 이미지 업로드",
            description = "사용자의 프로필 이미지를 업로드합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "업로드 성공",
                            content = @Content(mediaType = "application/json", schema = @Schema(type = "string"))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청",
                            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{}")))
            }
    )
    @PostMapping(value = "/profile/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> uploadProfile(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("profileImage") MultipartFile profileImage) throws IOException {

        Long kakaoId = extractKakaoIdFromJwt(authHeader);
        UserProfileRequestDto requestDto = new UserProfileRequestDto(kakaoId, profileImage);
        String profileImageUrl = userService.uploadProfileImage(requestDto);
        return ResponseEntity.ok(profileImageUrl);
    }

    @Operation(
            summary = "마이페이지에서 닉네임 수정",
            description = "사용자의 닉네임을 수정합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "닉네임 수정 성공",
                            content = @Content(mediaType = "application/json",
                                    examples = @ExampleObject(value = """
                            {
                                "status": 200,
                                "message": "닉네임 수정 완료",
                                "newNickname": "새로운닉네임"
                            }
                            """))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청",
                            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{}")))
            }
    )
    @PatchMapping("/nickname")
    public ResponseEntity<Map<String, Object>> updateNickname(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam
            @Parameter(description = "새로운 닉네임", example = "헬로키티") String newNickname) {

        Long kakaoId = extractKakaoIdFromJwt(authHeader);
        Map<String, Object> response = new HashMap<>();
        try {
            String message = userService.updateNickname(kakaoId, newNickname);
            response.put("status", HttpStatus.OK.value());
            response.put("message", message);
            response.put("newNickname", newNickname);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("status", HttpStatus.BAD_REQUEST.value());
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @Operation(
            summary = "유저 닉네임 중복 체크",
            description = "사용자가 설정하려는 닉네임의 사용 가능 여부를 확인합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "사용 가능한 닉네임",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                    {
                        "available": true,
                        "message": "사용 가능한 닉네임입니다."
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "사용 불가능한 닉네임",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                    {
                        "available": false,
                        "message": "이미 사용 중인 닉네임입니다."
                    }
                    """
                            )
                    )
            )
    })
    @GetMapping("/user-nickname-check")
    public ResponseEntity<NicknameCheckResponseDto> checkUserNicknameAvailability(
            @RequestParam
            @Parameter(description = "사용자가 확인하려는 닉네임", example = "김민수")
            String userNickname) {
        if ("".equals(userNickname)) {
            return ResponseEntity.badRequest().body(
                    new NicknameCheckResponseDto(false, "닉네임을 입력해주세요.") // 빈 닉네임에 대한 메시지
            );
        }
        boolean isAvailable = userService.isUserNicknameAvailable(userNickname);
        if (isAvailable) {
            return ResponseEntity.ok(new NicknameCheckResponseDto(true, "사용 가능한 닉네임입니다."));
        } else {
            return ResponseEntity.badRequest().body(new NicknameCheckResponseDto(false, "이미 사용 중인 닉네임입니다."));
        }
    }

    @Operation(
            summary = "온보딩 닉네임 설정",
            description = "사용자의 닉네임을 설정하고 온보딩을 완료합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "닉네임 설정 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                    {
                        "status": 200,
                        "data": {
                            "id" : 1,
                            "kakaoId": 12345678,
                            "nickname": "카카오닉네임",
                            "userNickname": "설정된닉네임",
                            "profileImageUrl": "http://..."
                        },
                        "message": "닉네임 설정 완료"
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                    {
                        "status": 400,
                        "message": "권한 문제"
                    }
                    """
                            )
                    )
            )
    })
    @PostMapping("/api/onboarding")
    public ResponseEntity<Map<String, Object>> onboardUser(
            @RequestBody OnboardingRequestDto onboardingRequestDto,
            @RequestHeader("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", 400,
                    "message", "권한 문제"
            ));
        }

        try {
            String token = authHeader.substring(7);
            Long kakaoId = Long.valueOf(jwtUtil.extractSubject(token));

            // 사용자 온보딩 처리
            User user = userService.onboardUser(kakaoId, onboardingRequestDto);

            return ResponseEntity.ok(Map.of(
                    "status", 200,
                    "message", "닉네임 설정 완료",
                    "data", user
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(Map.of(
                    "status", 400,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "status", 500,
                    "message", "서버 에러"
            ));
        }
    }


    @Operation(
            summary = "JWT를 검증하여 카카오 ID로 사용자 정보 조회",
            description = "JWT 토큰으로 사용자 인증 후 정보를 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "사용자 정보 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                    {
                        "status": 200,
                        "data": {
                            "kakaoId": 12345678,
                            "userName": "카카오닉네임",
                            "userNickname": "앱설정닉네임",
                            "profileImageUrl": "http://..."
                        }
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                    {
                        "status": 401,
                        "message": "JWT가 만료되었습니다."
                    }
                    """
                            )
                    )
            )
    })
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getUserInfoFromJwt(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("Invalid or missing Authorization header");
            return ResponseEntity.badRequest().body(Map.of(
                    "status", 400,
                    "message", "유효하지 않는 토큰"
            ));
        }

        String token = authHeader.substring(7); // "Bearer " 이후의 토큰만 추출
        try {
            // JWT 유효성 검증 및 사용자 ID 추출
            String kakaoIdStr = jwtUtil.extractSubject(token); // JWT의 subject에서 카카오 ID 추출
            Long kakaoId = Long.valueOf(kakaoIdStr);

            // 카카오 ID로 사용자 정보 조회
            User user = userService.getUserByKakaoId(kakaoId)
                    .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자"));

            // 사용자 정보를 응답으로 반환
            Map<String, Object> response = Map.of(
                    "status", 200,
                    "data", Map.of(
                            "kakaoId", user.getKakaoId(),
                            "userName", user.getNickname(),
                            "userNickname", user.getUserNickname() != null ? user.getUserNickname() : "",
                            "profileImageUrl", user.getProfileImageUrl()
                    )
            );


            return ResponseEntity.ok(response);

        } catch (NumberFormatException e) {
            logger.error("카카오 ID 변환 오류: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "status", 400,
                    "message", "카카오 ID 형식이 올바르지 않습니다."
            ));
        } catch (ExpiredJwtException e) {
            logger.error("JWT 만료: ", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "status", 401,
                    "message", "JWT가 만료되었습니다."
            ));
        } catch (SignatureException e) {
            logger.error("JWT 서명 오류: ", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "status", 401,
                    "message", "JWT 서명이 유효하지 않습니다."
            ));
        } catch (MalformedJwtException e) {
            logger.error("JWT 형식 오류: ", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "status", 401,
                    "message", "JWT 형식이 올바르지 않습니다."
            ));
        } catch (RuntimeException e) {
            logger.error("사용자 조회 오류: ", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "status", 404,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            logger.error("예상치 못한 오류: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", 500,
                    "message", "서버 오류가 발생했습니다."
            ));
        }
    }

}
