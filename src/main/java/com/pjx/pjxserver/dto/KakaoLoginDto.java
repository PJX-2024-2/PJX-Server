package com.pjx.pjxserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
@Getter
public class KakaoLoginDto {
    private String accessToken;
    private String refreshToken;
}
