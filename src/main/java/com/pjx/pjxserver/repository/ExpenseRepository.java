package com.pjx.pjxserver.repository;

import com.pjx.pjxserver.domain.Expense;
import com.pjx.pjxserver.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByUserAndDateBetween(User user, LocalDate start, LocalDate end);
    List<Expense> findByUserAndDate(User user, LocalDate date);

}
