package com.pjx.pjxserver.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "spending_goal")
public class SpendingGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private BigDecimal monthlyGoal;

    @Column(nullable = false)
    private BigDecimal currentSpending = BigDecimal.ZERO;

    @Column(nullable = false)
    private LocalDate goalDate;

//    public void setmonthlyGoal(BigDecimal goal) {
//    }

    public void setmonthlyGoal(BigDecimal monthlyGoal) {
        this.monthlyGoal = monthlyGoal;
    }

    public void setCurrentSpending(BigDecimal currentSpending) {
        this.currentSpending = currentSpending;
    }

    public void setMonthlyGoal(BigDecimal monthlyGoal) {
        this.monthlyGoal = monthlyGoal;
    }
}
