package com.pjx.pjxserver.service;

import com.pjx.pjxserver.dto.OnboardingRequestDto;
import com.pjx.pjxserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import com.pjx.pjxserver.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public boolean isNicknameAvailable(String nickname) {
        return userRepository.findByNickname(nickname).isEmpty();
    }


    @Transactional
    public User onboardUser(OnboardingRequestDto onboardingRequest) {
        if (!isNicknameAvailable(onboardingRequest.getNickname())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다."); // "Nickname already in use."
        }


        User user = User.builder()
                .kakaoId(onboardingRequest.getKakaoId())
                .nickname(onboardingRequest.getNickname())
                .build();

        return userRepository.save(user);
    }
}