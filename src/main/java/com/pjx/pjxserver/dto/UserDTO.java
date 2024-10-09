package com.pjx.pjxserver.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Data
public class UserDTO {
    private Long id; // 추가: 사용자 ID
    private String kakaoLoginId; // 카카오 로그인 ID
    private String email; // 이메일
    private String nickname; // 닉네임
    private Character gender; // 성별
    private LocalDate birthdate; // 생년월일
//    private BigDecimal annualIncome; // 연간 소득
}
