package com.pjx.pjxserver.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NicknameCheckResponseDto {
    private boolean available;
    private String message;
}