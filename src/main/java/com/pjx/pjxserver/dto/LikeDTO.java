package com.pjx.pjxserver.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class LikeDTO {
    private Long id;
    private Long expenseId;
    private Long likerId;
    private LocalDateTime createdAt;
}
