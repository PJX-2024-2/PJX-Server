package com.pjx.pjxserver.controller;


import org.springframework.http.server.reactive.ServerHttpRequest;
import com.pjx.pjxserver.common.JwtUtil;
import com.pjx.pjxserver.domain.User;
import com.pjx.pjxserver.dto.KakaoTokenResponseDto;
import com.pjx.pjxserver.service.KakaoService;
import com.pjx.pjxserver.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Tag(name = "카카오", description = "카카오 로그인 API")
public class KakaoLoginController {


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
            )
    })
   @GetMapping("/api/kakao/callback")
public Mono<ResponseEntity<KakaoTokenResponseDto>> kakaoCallback(
        @RequestParam
        @Parameter(description = "카카오로부터 받은 인증 코드") String code,
        ServerHttpRequest request) {
    // HTTP 요청의 Origin 헤더 가져오기
    String origin = request.getHeaders().getFirst("Origin");
    return kakaoService.getAccessToken(code, origin)
            .map(ResponseEntity::ok)
            .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().build()));
}

    @Operation(
            summary = "Access Token으로 카카오 유저 정보 가져오고 사용자 저장",
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
public Mono<ResponseEntity<Map<String, Object>>> saveOrUpdateUserInfo(
            @RequestParam
            @Parameter(description = "카카오 액세스 토큰")
            String accessToken) {
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
}
