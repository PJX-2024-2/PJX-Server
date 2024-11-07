package com.pjx.pjxserver.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Spending {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long kakaoId; // 사용자 식별을 위한 Kakao ID

    @Column(nullable = false)
    private BigDecimal amount; // 지출 금액

    @Column(nullable = false)
    private String description; // 지출 내용

    private String note; // 추가 메모

    @ElementCollection
    private List<String> images; // 이미지 파일 경로 또는 URL 목록

    @Column(nullable = false)
    private LocalDate date; // 지출 날짜

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user; // 지출을 기록한 사용자

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }
}
