package com.pjx.pjxserver.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class KakaoUserInfoResponseDto {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("properties")
    private Properties properties;

    @Getter
    public static class Properties {

        @JsonProperty("nickname")
        private String nickname;

        @JsonProperty("profile_image")
        private String profileImage;
    }
}
