package com.pjx.pjxserver.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.pjx.pjxserver.dto.OnboardingRequestDto;
import com.pjx.pjxserver.dto.UserProfileRequestDto;
import com.pjx.pjxserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import com.pjx.pjxserver.domain.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    public String getProfileImageUrl(Long kakaoId) {
        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음."));
        return user.getProfileImageUrl();
    }

    public String uploadProfileImage(UserProfileRequestDto requestDto) throws IOException {
        MultipartFile file = requestDto.getProfileImage();
        String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename();
        String filePath = "profiles/" + fileName;

        // 파일 확장자에 따른 Content-Type 설정
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(getContentType(file.getOriginalFilename()));

        amazonS3Client.putObject(bucketName, filePath, file.getInputStream(), metadata);

        String fileUrl = amazonS3Client.getUrl(bucketName, filePath).toString();
        User user = userRepository.findByKakaoId(requestDto.getKakaoId())
                .orElse(User.builder()
                        .kakaoId(requestDto.getKakaoId())
                        .build());
        user = User.builder()
                .id(user.getId())
                .kakaoId(user.getKakaoId())
                .nickname(user.getNickname())
                .profileImageUrl(fileUrl)
                .build();
        userRepository.save(user);
        return fileUrl;
    }

    public void deleteProfileImage(Long kakaoId) {
        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음."));
        String filePath = user.getProfileImageUrl().substring(user.getProfileImageUrl().indexOf("profiles/"));
        amazonS3Client.deleteObject(bucketName, filePath);
        user = User.builder()
                .id(user.getId())
                .kakaoId(user.getKakaoId())
                .nickname(user.getNickname())
                .profileImageUrl(null)
                .build();
        userRepository.save(user);
    }

    public boolean isNicknameAvailable(String nickname) {
        return userRepository.findByNickname(nickname).isEmpty();
    }

    @Transactional
    public User onboardUser(OnboardingRequestDto onboardingRequest) {
        if (!isNicknameAvailable(onboardingRequest.getNickname())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        User user = User.builder()
                .kakaoId(onboardingRequest.getKakaoId())
                .nickname(onboardingRequest.getNickname())
                .build();

        return userRepository.save(user);
    }

    // 파일 확장자에 따른 Content-Type 반환 메서드
    private String getContentType(String fileName) {
        if (fileName.endsWith(".webp")) {
            return "image/webp";
        } else if (fileName.endsWith(".png")) {
            return "image/png";
        } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else {
            return "application/octet-stream"; // 기본값
        }
    }
}
