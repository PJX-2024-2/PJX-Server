package com.pjx.pjxserver.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.pjx.pjxserver.domain.User;
import com.pjx.pjxserver.dto.OnboardingRequestDto;
import com.pjx.pjxserver.dto.UserProfileRequestDto;
import com.pjx.pjxserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

import com.pjx.pjxserver.domain.Feed;
import com.pjx.pjxserver.domain.Friend;
import com.pjx.pjxserver.repository.FeedRepository;
import com.pjx.pjxserver.repository.FriendRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AmazonS3Client amazonS3Client;
    private final FriendRepository friendRepository;
    private final FeedRepository feedRepository;

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
    //========================
    public List<User> searchUsersByNickname(String nickname) {
        return userRepository.findByNicknameContaining(nickname);
    }

    // 친구 추가 메서드
    public String addFriendByKakaoId(Long userKakaoId, String friendNickname, Long friendKakaoId) {
        // 친구 조회 로직 추가 (닉네임이나 카카오 ID를 통해)
        User user = userRepository.findByKakaoId(userKakaoId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        User friend;
        if (friendKakaoId != null) {
            friend = userRepository.findByKakaoId(friendKakaoId)
                    .orElseThrow(() -> new IllegalArgumentException("친구를 찾을 수 없습니다."));
        } else {
            friend = userRepository.findByNickname(friendNickname)
                    .orElseThrow(() -> new IllegalArgumentException("해당 닉네임의 친구를 찾을 수 없습니다."));
        }

        // 친구 관계 확인 및 저장
        if (friendRepository.existsByUserAndFriend(user, friend)) {
            throw new IllegalArgumentException("이미 친구로 등록된 사용자입니다.");
        }

        friendRepository.save(new Friend(user, friend));
        return "친구가 성공적으로 추가되었습니다.";
    }


    public boolean isOwnFeed(Long userKakaoId, Long targetKakaoId) {
        // userKakaoId와 targetKakaoId를 비교하여 본인 피드인지 확인
        return userKakaoId.equals(targetKakaoId);
    }

    public Optional<User> getUserByKakaoId(Long kakaoId) {
        return userRepository.findByKakaoId(kakaoId);
    }

    public List<Feed> getUserFeed(Long userKakaoId, Long targetKakaoId) {
        User targetUser = userRepository.findByKakaoId(targetKakaoId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        // 팔로우 상태 확인
        boolean isFollowing = isFollowing(userKakaoId, targetKakaoId);

        // 팔로우하지 않은 경우 제한된 피드만 반환하도록 처리
        if (!isFollowing && !userKakaoId.equals(targetKakaoId)) {
            throw new IllegalArgumentException("팔로우하지 않은 사용자의 피드에 접근할 수 없습니다.");
        }

        return feedRepository.findByUser(targetUser);
    }

    public boolean isFollowing(Long userKakaoId, Long friendKakaoId) {
        User user = userRepository.findByKakaoId(userKakaoId).orElseThrow();
        User friend = userRepository.findByKakaoId(friendKakaoId).orElseThrow();
        return friendRepository.existsByUserAndFriend(user, friend);
    }

    @Transactional
    public String followUser(Long userKakaoId, Long friendKakaoId) {
        User user = userRepository.findByKakaoId(userKakaoId).orElseThrow();
        User friend = userRepository.findByKakaoId(friendKakaoId).orElseThrow();

        if (friendRepository.existsByUserAndFriend(user, friend)) {
            throw new IllegalArgumentException("이미 팔로우 중입니다.");
        }

        friendRepository.save(new Friend(user, friend));
        return "팔로우 성공";
    }

    @Transactional
    public String unfollowUser(Long userKakaoId, Long friendKakaoId) {
        User user = userRepository.findByKakaoId(userKakaoId).orElseThrow();
        User friend = userRepository.findByKakaoId(friendKakaoId).orElseThrow();

        friendRepository.deleteByUserAndFriend(user, friend);
        return "언팔로우 성공";
    }
}
