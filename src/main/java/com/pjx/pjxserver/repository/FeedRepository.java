package com.pjx.pjxserver.repository;

import com.pjx.pjxserver.domain.Feed;
import com.pjx.pjxserver.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedRepository extends JpaRepository<Feed, Long> {
    // 특정 사용자의 피드 목록 조회
    List<Feed> findByUser(User user);
}