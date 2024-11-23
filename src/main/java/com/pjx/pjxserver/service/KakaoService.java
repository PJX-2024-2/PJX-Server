package com.pjx.pjxserver.service;

import com.pjx.pjxserver.dto.KakaoTokenResponseDto;
import com.pjx.pjxserver.dto.KakaoUserInfoResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoService {

    @Value("${kakao.client_id}")
    private String clientId;

    @Value("${kakao.client_secret}")
    private String clientSecret;

    @Value("${kakao.redirect_uri.local}")
    private String localRedirectUri;

    @Value("${kakao.redirect_uri.prod}")
    private String prodRedirectUri;

    private final WebClient.Builder webClientBuilder;


    public Mono<KakaoTokenResponseDto> getAccessToken(String code, String origin) {
        String redirectUri = determineRedirectUri(origin);
        log.info("Using redirect_uri: {}", redirectUri);

        return webClientBuilder.build()
                .post()
                .uri("https://kauth.kakao.com/oauth/token")
                .header(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded")
                .bodyValue("grant_type=authorization_code&client_id=" + clientId +
                        "&redirect_uri=" + redirectUri +
                        "&code=" + code +
                        "&client_secret=" + clientSecret)
                .retrieve()
                .bodyToMono(KakaoTokenResponseDto.class);
    }

    public Mono<KakaoUserInfoResponseDto> getUserInfo(String accessToken) {
        return webClientBuilder.build()
                .get()
                .uri("https://kapi.kakao.com/v2/user/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(KakaoUserInfoResponseDto.class);
    }

    private String determineRedirectUri(String origin) {
        if (origin == null) {
            log.warn("Origin header is missing. Defaulting to prod redirect_uri.");
            return prodRedirectUri; // 기본값으로 배포 환경 URI 사용
        }

        if (origin.contains("localhost")) {
            return localRedirectUri; // 로컬 환경
        }

        return prodRedirectUri; // 배포 환경
    }
}
