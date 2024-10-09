package com.pjx.pjxserver.repository;

import com.pjx.pjxserver.domain.AIExpenseRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AIExpenseRecordRepository extends JpaRepository<AIExpenseRecord, Long> {
}

