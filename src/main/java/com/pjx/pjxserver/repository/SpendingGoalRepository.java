package com.pjx.pjxserver.repository;

import com.pjx.pjxserver.domain.SpendingGoal;
import com.pjx.pjxserver.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface SpendingGoalRepository extends JpaRepository<SpendingGoal, Long> {
    Optional<SpendingGoal> findByUserAndGoalDate(User user, LocalDate goalDate);
}
