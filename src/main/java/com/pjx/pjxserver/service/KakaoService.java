package com.pjx.pjxserver.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private final ObjectMapper objectMapper;

    @Value("${kakao.client_id}")
    private String clientId;

    @Value("${kakao.client_secret}")
    private String clientSecret;

    @Value("${kakao.redirect_uri.local}")
    private String localRedirectUri;

    @Value("${kakao.redirect_uri.prod}")
    private String prodRedirectUri;

    private final WebClient.Builder webClientBuilder;

    

    public Mono<KakaoUserInfoResponseDto> getUserInfo(String accessToken) {
        return webClientBuilder.build()
                .get()
                .uri("https://kapi.kakao.com/v2/user/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(KakaoUserInfoResponseDto.class);
    }

    private String determineRedirectUri(String origin) {
        if (origin != null && origin.contains("localhost:5173")) {
            log.debug("Using local redirect URI for origin: {}", origin);
            return localRedirectUri;
        }
        log.debug("Using production redirect URI");
        return prodRedirectUri;
    }
}
