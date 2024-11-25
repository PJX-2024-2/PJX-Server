package com.pjx.pjxserver.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;


@Data
@Getter
@Setter
public class KakaoCallbackRequestDto {
    private String code;
}

