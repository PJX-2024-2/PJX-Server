package com.pjx.pjxserver.controller;


import com.pjx.pjxserver.common.JwtUtil;
import com.pjx.pjxserver.domain.User;
import com.pjx.pjxserver.dto.KakaoCallbackRequestDto;
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
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "카카오", description = "카카오 로그인 API")
public class KakaoLoginController {


    private final KakaoService kakaoService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Value("${kakao.client_id}")
    private String clientId;

        @Operation(
            summary = "백엔드를 위한 Kakao Login Page URL (배포 URI)",
            description = "카카오 로그인 페이지 URL을 반환합니다.",
            security = @SecurityRequirement(name = "")
    )
    @GetMapping("/api/kakao/login/dev")
    public ResponseEntity<String> kakaoLoginPageDev() {

        final String redirectUri ="https://pjx-client.vercel.app/auth/kakao";

        String loginUrl = "https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=" +
                clientId + "&redirect_uri=" + redirectUri;
        return ResponseEntity.ok(loginUrl);
    }

    @Operation(
            summary = "백엔드를 위한 Kakao Login Page URL (로컬 URI)",
            description = "카카오 로그인 페이지 URL을 반환합니다.",
            security = @SecurityRequirement(name = "")
    )
    @GetMapping("/api/kakao/login/local")
    public ResponseEntity<String> kakaoLoginPageLocal() {
        final String redirectUri = "http://localhost:5173/auth/kakao";

        String loginUrl = "https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=" +
                clientId + "&redirect_uri=" + redirectUri;
        return ResponseEntity.ok(loginUrl);
    }


     @PostMapping("/api/kakao/callback")
    public Mono<ResponseEntity<KakaoTokenResponseDto>> kakaoCallback(
            @RequestBody
            @Parameter(description = "카카오로부터 받은 인증 코드 및  Redirect URI")
            KakaoCallbackRequestDto requestDto
    ) {
        String code = requestDto.getCode(); // JSON에서 인증 코드 추출
        String redirectUri = requestDto.getRedirectUri(); // JSON에서 URI  추출

        return kakaoService.getAccessToken(code, redirectUri)
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
