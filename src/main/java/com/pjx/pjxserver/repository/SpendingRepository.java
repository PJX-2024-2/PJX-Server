package com.pjx.pjxserver.repository;

import com.pjx.pjxserver.domain.Spending;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SpendingRepository extends JpaRepository<Spending, Long> {
    // 특정 사용자의 친구 ID 목록을 기반으로 지출 내역 조회
//    List<Spending> findByUserIdIn(List<Long> userIds);

    @Query("SELECT s FROM Spending s WHERE s.kakaoId IN :kakaoIds")
    List<Spending> findByKakaoIdIn(List<Long> kakaoIds);

    List<Spending> findByKakaoIdAndDate(Long kakaoId, LocalDate date);

    @Query("SELECT s FROM Spending s WHERE s.kakaoId = :kakaoId AND s.date BETWEEN :startDate AND :endDate")
    List<Spending> findAllByKakaoIdAndDateBetween(@Param("kakaoId") Long kakaoId,
                                                  @Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);
}