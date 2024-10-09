package com.pjx.pjxserver.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Expenses")
@Getter
@Setter
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "expense_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    private BigDecimal amount;

    @Column(name = "expense_date")
    private LocalDate expenseDate;

    private String notes;

    @Column(name = "receipt_image")
    private String receiptImage;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "expense")
    private AIExpenseRecord aiExpenseRecord;

    @OneToMany(mappedBy = "expense")
    private Set<Feedback> feedbacks = new HashSet<>();

    @OneToMany(mappedBy = "expense")
    private Set<Like> likes = new HashSet<>();
}