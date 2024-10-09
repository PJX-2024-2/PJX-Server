package com.pjx.pjxserver.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Users")
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id; // 사용자 ID

    @Column(unique = true, nullable = false)
    private String email; // 이메일

    // 비밀번호  제거 해야하나 고민중
     @Column(nullable = false)
     private String password;

    @Column(nullable = false)
    private String nickname; // 닉네임

    @Column(length = 1)
    private Character gender; // 성별

    @Column(nullable = false)
    private LocalDate birthdate; // 생년월일

    @Column(name = "annual_income")
    private BigDecimal annualIncome; // 연간 소득

    @Column(name = "kakao_login_id", unique = true, nullable = false)
    private String kakaoLoginId; // 카카오 로그인 ID

    @Column(name = "created_at")
    private LocalDateTime createdAt; // 생성일

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // 수정일

    @OneToMany(mappedBy = "user")
    private Set<MonthlyExpenseGoal> monthlyExpenseGoals = new HashSet<>(); // 월간 지출 목표

    @OneToMany(mappedBy = "user")
    private Set<Expense> expenses = new HashSet<>(); // 지출 목록

    @ManyToMany
    @JoinTable(
            name = "Friends",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "friend_id")
    )
    private Set<User> friends = new HashSet<>(); // 친구 목록
}
