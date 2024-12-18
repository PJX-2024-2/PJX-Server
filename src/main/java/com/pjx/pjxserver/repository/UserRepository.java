package com.pjx.pjxserver.repository;

import com.pjx.pjxserver.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByNickname(String nickname);
    Optional<User> findByKakaoId(Long kakaoId);
    List<User> findByNicknameContaining(String nickname);

    boolean existsByNickname(String nickname);


    Optional<User> findByUserNickname(String userNickname);
    boolean existsByUserNickname(String userNickname); // 중복 여부 체크

    
}
