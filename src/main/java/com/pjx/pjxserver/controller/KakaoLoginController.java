package com.pjx.pjxserver.controller;

import com.pjx.pjxserver.common.JwtUtil;
import com.pjx.pjxserver.dto.KakaoCallbackRequestDto;
import com.pjx.pjxserver.domain.User;
import com.pjx.pjxserver.dto.KakaoTokenResponseDto;
import com.pjx.pjxserver.dto.KakaoUserInfoResponseDto;
import com.pjx.pjxserver.dto.NicknameCheckResponseDto;
import com.pjx.pjxserver.dto.OnboardingRequestDto;
import com.pjx.pjxserver.service.KakaoService;
import com.pjx.pjxserver.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class KakaoLoginController {

    private final KakaoService kakaoService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Value("${kakao.client_id}")
    private String clientId;

    @Value("${kakao.redirect_uri}")
    private String redirectUri;

    @Operation(summary = "백엔드를 위한 Kakao Login Page URL", security = @SecurityRequirement(name = ""))
    @GetMapping("/api/kakao/login")
    public ResponseEntity<String> kakaoLoginPage() {
        String loginUrl = "https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=" +
                clientId + "&redirect_uri=" + redirectUri;
        return ResponseEntity.ok(loginUrl);
    }

    @Operation(summary = " Access Token과 Refresh Token을 얻기 위한 API", security = @SecurityRequirement(name = ""))
    @PostMapping("/api/kakao/callback")
    public Mono<ResponseEntity<KakaoTokenResponseDto>> kakaoCallback(@RequestBody KakaoCallbackRequestDto request) {
        String code = request.getCode();

        return kakaoService.getAccessToken(code)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().build()));
    }

    @Operation(summary = "Access Token으로 카카오 유저 정보 가져오기 및 JWT 발급", security = @SecurityRequirement(name = ""))
    @GetMapping("/api/kakao/userinfo")
    public Mono<ResponseEntity<Map<String, Object>>> getUserInfo(@RequestParam String accessToken) {
        return kakaoService.getUserInfo(accessToken)
                .flatMap(userInfo -> {
                    String jwtToken = jwtUtil.generateToken(Map.of(), userInfo.getId().toString());

                    Map<String, Object> response = Map.of(
                            "userInfo", userInfo,
                            "jwtToken", jwtToken
                    );

                    return Mono.just(ResponseEntity.ok(response));
                })
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().build()));
    }



    @Operation(summary = "닉네임 중복 체크")
    @GetMapping("/api/onboarding/nickname-check")
    public ResponseEntity<NicknameCheckResponseDto> checkNicknameAvailability(@RequestParam String nickname) {
        boolean isAvailable = userService.isNicknameAvailable(nickname);
        if (isAvailable) {
            return ResponseEntity.ok(new NicknameCheckResponseDto(true, "사용 가능한 닉네임입니다.")); // "Nickname is available."
        } else {
            return ResponseEntity.badRequest().body(new NicknameCheckResponseDto(false, "이미 사용 중인 닉네임입니다.")); // "Nickname already in use."
        }
    }

//    @Operation(summary = "온보딩 닉네임 설정 -> 회원")
//    @PostMapping("/api/onboarding")
//    public ResponseEntity<User> onboardUser(@RequestBody OnboardingRequestDto onboardingRequestDto,
//                                            @RequestHeader("Authorization") String authHeader) {
//        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//            return ResponseEntity.badRequest().body(null);
//        }
//
//        try {
//            String token = authHeader.substring(7);
//            Long kakaoId = Long.valueOf(jwtUtil.extractSubject(token));
//
//            User user = userService.onboardUser(kakaoId, onboardingRequestDto);
//            return ResponseEntity.ok(user);
//
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.status(400).body(null); // 닉네임 중복 시 400 반환
//        } catch (Exception e) {
//            return ResponseEntity.status(401).body(null); // 인증 실패 시 401 반환
//        }
//    }

    @Operation(summary = "온보딩 닉네임 설정 -> 회원")
    @PostMapping("/api/onboarding")
    public ResponseEntity<Map<String, Object>> onboardUser(
            @RequestBody OnboardingRequestDto onboardingRequestDto,
            @RequestHeader("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", 400,
                    "message", "Invalid or missing Authorization header"
            ));
        }

        try {
            String token = authHeader.substring(7);
            Long kakaoId = Long.valueOf(jwtUtil.extractSubject(token));

            User user = userService.onboardUser(kakaoId, onboardingRequestDto);

            return ResponseEntity.ok(Map.of(
                    "status", 200,
                    "message", "Onboarding completed successfully.",
                    "data", user
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(Map.of(
                    "status", 400,
                    "message", e.getMessage() // 예외 메시지 포함
            ));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of(
                    "status", 401,
                    "message", "Authentication failed"
            ));
        }
    }



    @Operation(summary = "JWT를 검증하여 카카오 ID로 사용자 정보 조회")
    @GetMapping("/api/users/me")
    public ResponseEntity<Map<String, Object>> getUserInfoFromJwt(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", 400,
                    "message", "Invalid or missing Authorization header"
            ));
        }

        try {
            // JWT에서 사용자 ID (카카오 ID) 추출
            String token = authHeader.substring(7); // "Bearer " 이후의 토큰만 추출
            String kakaoId = jwtUtil.extractSubject(token); // JWT의 subject에서 카카오 ID 추출

            // 카카오 ID로 사용자 정보 조회
            User user = userService.getUserByKakaoId(Long.valueOf(kakaoId))
                    .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자"));

            // 사용자 정보를 응답으로 반환
            Map<String, Object> response = Map.of(
                    "status", 200,
                    "data", Map.of(
                            "kakaoId", user.getKakaoId(),
                            "nickname", user.getNickname(),
                            "profileImageUrl", user.getProfileImageUrl()
                    )
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of(
                    "status", 401,
                    "message", "유효하지 않은 JWT"
            ));
        }
    }


}
