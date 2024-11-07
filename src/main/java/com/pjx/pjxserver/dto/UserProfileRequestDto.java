package com.pjx.pjxserver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileRequestDto {
    private Long kakaoId;
    private MultipartFile profileImage; // 프로필 이미지 파일
}
