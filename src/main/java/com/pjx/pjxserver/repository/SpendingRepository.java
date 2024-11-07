package com.pjx.pjxserver.repository;

import com.pjx.pjxserver.domain.Spending;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpendingRepository extends JpaRepository<Spending, Long> {
    // 특정 사용자의 친구 ID 목록을 기반으로 지출 내역 조회
//    List<Spending> findByUserIdIn(List<Long> userIds);

    @Query("SELECT s FROM Spending s WHERE s.kakaoId IN :kakaoIds")
    List<Spending> findByKakaoIdIn(List<Long> kakaoIds);
}
