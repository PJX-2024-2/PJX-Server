package com.pjx.pjxserver.domain;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long kakaoId; // 사용자 ID

    private LocalDate date; // 리액션 날짜

    private String reactionType; // 리액션 타입 (예: HAPPY, SAD 등)

    // Spending과 연결할 경우 추가 (지출과 독립적이라면 제거 가능)
    @ManyToOne
    @JoinColumn(name = "spending_id", nullable = true)
    private Spending spending;
}
