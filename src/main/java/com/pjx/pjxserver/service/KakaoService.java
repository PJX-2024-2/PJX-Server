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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
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

    public Mono<KakaoTokenResponseDto> getAccessToken(String code) {

        String redirectUri = determineRedirectUri();
        log.info("Using redirect_uri: {}", redirectUri);

        log.info("Request parameters: client_id={}, redirect_uri={}, code={}, client_secret={}",
                clientId, redirectUri, code, clientSecret);

        return webClientBuilder.build()
                .post()
                .uri("https://kauth.kakao.com/oauth/token")
                .header(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded")
                .bodyValue("grant_type=authorization_code&client_id=" + clientId +
                        "&redirect_uri=" + redirectUri +
                        "&code=" + code +
                        "&client_secret=" + clientSecret)
                .exchangeToMono(response -> {
                    log.info("Response status: {}", response.statusCode());
                    return response.bodyToMono(String.class)
                            .doOnNext(body -> log.info("Response body: {}", body))
                            .map(body -> {
                                try {
                                    return objectMapper.readValue(body, KakaoTokenResponseDto.class);
                                } catch (JsonProcessingException e) {
                                    throw new RuntimeException("Error parsing Kakao token response", e);
                                }
                            });
                })
                .doOnError(e -> log.error("Error getting access token from Kakao", e));
    }

    public Mono<KakaoUserInfoResponseDto> getUserInfo(String accessToken) {
        return webClientBuilder.build()
                .get()
                .uri("https://kapi.kakao.com/v2/user/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(KakaoUserInfoResponseDto.class);
    }

    private String determineRedirectUri() {
        String currentUri = ServletUriComponentsBuilder.fromCurrentRequest().toUriString();

        if (currentUri.contains("localhost")) {
            log.info("Using local redirect URI for current URI: {}", currentUri);
            return localRedirectUri;
        }
        log.info("Using prod redirect URI for current URI: {}", currentUri);
        return prodRedirectUri;
    }
}
