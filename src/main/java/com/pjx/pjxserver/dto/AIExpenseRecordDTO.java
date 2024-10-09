package com.pjx.pjxserver.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AIExpenseRecordDTO {
    private Long id;
    private Long expenseId;
    private String receiptImage;
    private String parsedText;
    private LocalDateTime createdAt;
}
