package com.pjx.pjxserver.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UserCreateDTO {
    private String email;
    private String password;
    private String nickname;
    private Character gender;
    private LocalDate birthdate;
    private BigDecimal annualIncome;
    private String kakaoLoginId;
}