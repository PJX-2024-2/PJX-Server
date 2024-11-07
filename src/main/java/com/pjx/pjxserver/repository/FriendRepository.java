package com.pjx.pjxserver.repository;

import com.pjx.pjxserver.domain.Friend;
import com.pjx.pjxserver.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend, Long> {
    // 두 사용자가 이미 친구 관계인지 확인
    boolean existsByUserAndFriend(User user, User friend);

    // 특정 사용자가 특정 친구를 팔로우하고 있는지 조회
    Optional<Friend> findByUserAndFriend(User user, User friend);

    // 팔로우 관계 삭제
    void deleteByUserAndFriend(User user, User friend);

    @Query("SELECT f.friend.kakaoId FROM Friend f WHERE f.user.kakaoId = :kakaoId")
    List<Long> findFriendIdsByKakaoId(Long kakaoId);

    // 두 사용자가 친구 관계인지 확인
    boolean existsByUser_KakaoIdAndFriend_KakaoId(Long userKakaoId, Long friendKakaoId);
}
