package com.pjx.pjxserver.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MonthlyExpenseGoalDTO {
    private Long id;
    private Long userId;
    private Integer year;
    private Integer month;
    private BigDecimal foodBudget;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
