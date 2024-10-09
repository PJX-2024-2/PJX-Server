package com.pjx.pjxserver.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "AI_Expense_Records")
@Getter
@Setter
public class AIExpenseRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ai_record_id")
    private Long id;

    @OneToOne
    @JoinColumn(name = "expense_id")
    private Expense expense;

    @Column(name = "receipt_image", nullable = false)
    private String receiptImage;

    @Column(name = "parsed_text", nullable = false)
    private String parsedText;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}