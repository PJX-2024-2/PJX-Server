package com.pjx.pjxserver.repository;

import com.pjx.pjxserver.domain.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByExpenseId(Long expenseId);
}