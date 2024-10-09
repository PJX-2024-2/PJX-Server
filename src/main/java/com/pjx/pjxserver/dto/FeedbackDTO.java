package com.pjx.pjxserver.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FeedbackDTO {
    private Long id;
    private Long expenseId;
    private Long commenterId;
    private String comment;
    private LocalDateTime createdAt;
}
