package com.pjx.pjxserver.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long kakaoId;

    @Column(unique = true, nullable = false)
    private String nickname;// 카카오에서 제공하는 닉네임

    @Column
    private String userNickname; // 애플리케이션 닉네임

    private String profileImageUrl;
}
