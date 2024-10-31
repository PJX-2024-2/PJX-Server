package com.pjx.pjxserver.controller;


import com.pjx.pjxserver.domain.User;
import com.pjx.pjxserver.dto.KakaoTokenResponseDto;
import com.pjx.pjxserver.dto.KakaoUserInfoResponseDto;
import com.pjx.pjxserver.dto.NicknameCheckResponseDto;
import com.pjx.pjxserver.dto.OnboardingRequestDto;
import com.pjx.pjxserver.service.KakaoService;
import com.pjx.pjxserver.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class KakaoLoginController {

    private final KakaoService kakaoService;
    private final UserService userService;

    @Value("${kakao.client_id}")
    private String clientId;

    @Value("${kakao.redirect_uri}")
    private String redirectUri;

    @Operation(summary = "백엔드를 위한 Kakao Login Page URL")
    @GetMapping("/api/kakao/login")
    public ResponseEntity<String> kakaoLoginPage() {
        String loginUrl = "https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=" +
                clientId + "&redirect_uri=" + redirectUri;
        return ResponseEntity.ok(loginUrl);
    }

    @Operation(summary = " Access Token과 Refresh Token을 얻기 위한 API")
    @GetMapping("/api/kakao/callback")
    public Mono<ResponseEntity<KakaoTokenResponseDto>> kakaoCallback(@RequestParam String code) {
        return kakaoService.getAccessToken(code)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().build()));
    }


    @Operation(summary = "Access Token으로 카카오 유저 정보 가져오는 API")
    @GetMapping("/api/kakao/userinfo")
    public Mono<ResponseEntity<KakaoUserInfoResponseDto>> getUserInfo(@RequestParam String accessToken) {
        return kakaoService.getUserInfo(accessToken)
                .map(ResponseEntity::ok)
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

    @Operation(summary = "온보딩 닉네임 설정 -> 회원")
    @PostMapping("/api/onboarding")
    public ResponseEntity<User> onboardUser(@RequestBody OnboardingRequestDto onboardingRequestDto) {
        User user = userService.onboardUser(onboardingRequestDto);
        return ResponseEntity.ok(user); // "Onboarding completed successfully."
    }

}
