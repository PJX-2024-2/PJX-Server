package com.pjx.pjxserver.repository;

import com.pjx.pjxserver.domain.Reaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReactionRepository extends JpaRepository<Reaction, Long> {
    Optional<Reaction> findByKakaoIdAndDate(Long kakaoId, LocalDate date);

    List<Reaction> findAllByKakaoIdAndDateBetween(Long kakaoId, LocalDate startDate, LocalDate endDate);
}
