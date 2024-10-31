package com.pjx.pjxserver.repository;

import com.pjx.pjxserver.domain.SpendingGoal;
import com.pjx.pjxserver.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface SpendingGoalRepository extends JpaRepository<SpendingGoal, Long> {
    Optional<SpendingGoal> findByUserAndGoalDate(User user, LocalDate goalDate);
}