package com.pjx.pjxserver.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ExpenseDTO {
    private Long id;
    private Long userId;
    private Long categoryId;
    private BigDecimal amount;
    private LocalDate expenseDate;
    private String notes;
    private String receiptImage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
