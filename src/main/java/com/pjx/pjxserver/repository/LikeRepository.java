package com.pjx.pjxserver.repository;

import com.pjx.pjxserver.domain.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    List<Like> findByExpenseId(Long expenseId);
}