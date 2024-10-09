package com.pjx.pjxserver.service;

import com.pjx.pjxserver.domain.User;
import com.pjx.pjxserver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    // 사용자 ID로 사용자 조회
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // 사용자 생성 (회원가입)
    public User createUser(User user) {
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    // 카카오 로그인 처리
    public User loginWithKakao(String kakaoToken) {
        // 여기서 kakaoToken을 사용하여 카카오 API를 호출하고 사용자 정보를 가져오는 로직을 구현합니다.
        // 예를 들어, kakaoToken을 사용하여 카카오 사용자 ID를 가져올 수 있습니다.

        // 가상의 카카오 로그인 ID로 사용자 조회
        String kakaoLoginId = getKakaoLoginIdFromToken(kakaoToken); // 구현 필요
        User user = userRepository.findByKakaoLoginId(kakaoLoginId);

        // 사용자가 존재하지 않으면 새로운 사용자 생성
        if (user == null) {
            user = new User();
            user.setKakaoLoginId(kakaoLoginId);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            // 필요한 다른 사용자 정보 설정
            return userRepository.save(user);
        }

        // 이미 존재하는 사용자 반환
        return user;
    }

    // 카카오 토큰에서 로그인 ID를 가져오는 메서드 (구현 필요)
    private String getKakaoLoginIdFromToken(String kakaoToken) {
        // 카카오 API 호출 -> kakaoLoginId를 반환
        return "example_kakao_login_id";
    }
}
