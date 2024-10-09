package com.pjx.pjxserver.repository;

import com.pjx.pjxserver.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);

    // 카카오 로그인 ID로 사용자 조회
    User findByKakaoLoginId(String kakaoLoginId);
}
