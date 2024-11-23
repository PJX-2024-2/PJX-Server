package com.pjx.pjxserver.controller;

import com.pjx.pjxserver.common.JwtUtil;
import com.pjx.pjxserver.domain.User;
import com.pjx.pjxserver.dto.KakaoTokenResponseDto;
import com.pjx.pjxserver.dto.NicknameCheckResponseDto;
import com.pjx.pjxserver.dto.OnboardingRequestDto;
import com.pjx.pjxserver.service.KakaoService;
import com.pjx.pjxserver.service.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Tag(name = "카카오", description = "카카오 로그인 API")
public class KakaoLoginController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final KakaoService kakaoService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Value("${kakao.client_id}")
    private String clientId;

    @Value("${kakao.redirect_uri}")
    private String redirectUri;

    @Operation(
            summary = "백엔드를 위한 Kakao Login Page URL",
            description = "카카오 로그인 페이지 URL을 반환합니다.",
            security = @SecurityRequirement(name = "")
    )
    @ApiResponse(
            responseCode = "200",
            description = "로그인 URL 반환 성공",
            content = @Content(
                    mediaType = "text/plain",
                    schema = @Schema(type = "string"),
                    examples = @ExampleObject(
                            value = "https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=your_client_id&redirect_uri=your_redirect_uri"
                    )
            )
    )
    @GetMapping("/api/kakao/login")
    public ResponseEntity<String> kakaoLoginPage() {
        String loginUrl = "https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=" +
                clientId + "&redirect_uri=" + redirectUri;
        return ResponseEntity.ok(loginUrl);
    }


    @Operation(
            summary = "Access Token과 Refresh Token을 얻기 위한 API",
            description = "카카오 인증 코드로 액세스 토큰과 리프레시 토큰을 발급받습니다.",
            security = @SecurityRequirement(name = "")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "토큰 발급 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = KakaoTokenResponseDto.class),
                            examples = @ExampleObject(
                                    value = """
                    {
                        "access_token": "access_token_value",
                        "refresh_token": "refresh_token_value",
                        "expires_in": 21599,
                        "refresh_token_expires_in": 5183999
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
                            examples = @ExampleObject(value = "{}")
                    )
            )
    })
    @GetMapping("/api/kakao/callback")
    public Mono<ResponseEntity<KakaoTokenResponseDto>> kakaoCallback(@RequestParam String code) {
        return kakaoService.getAccessToken(code)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().build()));
    }

    @Operation(
            summary = "Access Token으로 카카오 유저 정보 가져오기 및 사용자 저장",
            description = "카카오 액세스 토큰으로 사용자 정보를 조회하고 저장합니다.",
            security = @SecurityRequirement(name = "")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "사용자 정보 조회/저장 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                    {
                        "message": "새로운 회원이 생성되었습니다. 또는 기존 회원 정보가 업데이트되었습니다.",
                        "jwtToken": "eyJhbGciOiJS...",
                        "status": "new 또는 existing",
                        "userInfo": {
                            "id": "유저 아이디",
                            "kakaoId": "카카오 아이디",
                            "nickname": "카카오닉네임",
                            "userNickname": "null 또는 앱 설정 닉네임",
                            "profileImageUrl": "http://..."
                        }
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
                        "status": "error",
                        "error": "오류 메시지",
                        "message": "사용자 정보를 처리하는 중 문제가 발생했습니다."  
                    }
                    """
                            )
                    )
            )
    })
@GetMapping("/api/kakao/userinfo")
public Mono<ResponseEntity<Map<String, Object>>> saveOrUpdateUserInfo(@RequestParam String accessToken) {
    return kakaoService.getUserInfo(accessToken)
            .flatMap(userInfo -> {
                // 사용자 정보 저장/업데이트
                Map<String, Object> result = userService.saveOrUpdateUser(
                        userInfo.getId(),
                        userInfo.getProperties().getNickname(), // 카카오 닉네임
                        userInfo.getProperties().getUserNickname(), // 애플리케이션에서 설정한 닉네임
                        userInfo.getProperties().getProfileImage() // 프로필 이미지
                );

                // 응답 데이터 생성
                String status = (String) result.get("status");
                String message = (String) result.get("message");
                User user = (User) result.get("user");

                // JWT 생성
                String jwtToken = jwtUtil.generateToken(Map.of(), user.getKakaoId().toString());

                // 최종 응답에 JWT 포함
                Map<String, Object> response = Map.of(
                        "status", status,
                        "message", message,
                        "userInfo", user,
                        "jwtToken", jwtToken
                );

                return Mono.just(ResponseEntity.ok(response));
            })
            .onErrorResume(e -> {
                // 에러 처리
                return Mono.just(ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "사용자 정보를 처리하는 중 문제가 발생했습니다.",
                        "error", e.getMessage()
                )));
            });
}

//    @Operation(summary = "유저 닉네임 중복 체크")
//    @GetMapping("/user-nickname-check")
//    public ResponseEntity<NicknameCheckResponseDto> checkUserNicknameAvailability(@RequestParam String userNickname) {
//        boolean isAvailable = userService.isUserNicknameAvailable(userNickname);
//        if (isAvailable) {
//            return ResponseEntity.ok(new NicknameCheckResponseDto(true, "사용 가능한 닉네임입니다.")); // 닉네임 사용 가능
//        } else {
//            return ResponseEntity.badRequest().body(new NicknameCheckResponseDto(false, "이미 사용 중인 닉네임입니다.")); // 닉네임 사용 불가능
//        }
//    }


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
public ResponseEntity<NicknameCheckResponseDto> checkUserNicknameAvailability(@RequestParam String userNickname) {
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
                            "id" : 1
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
