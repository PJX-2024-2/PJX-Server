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
import java.util.Map;
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
    public User onboardUser(Long kakaoId, OnboardingRequestDto onboardingRequest) {
        String newNickname = onboardingRequest.getUserNickname();

        if (newNickname == null || newNickname.trim().isEmpty()) {
            throw new IllegalArgumentException("닉네임을 입력하세요.");
        }

        Optional<User> existingUser = userRepository.findByKakaoId(kakaoId);

        if (existingUser.isPresent()) {
            User user = existingUser.get();

            // 기존 닉네임이 빈 문자열일 경우에만 새 닉네임 설정 허용
            if (user.getUserNickname() == null || user.getUserNickname().trim().isEmpty()) {
                user.setUserNickname(newNickname);
                return userRepository.save(user);
            }

            // 기존 닉네임이 설정된 경우 예외 처리
            throw new IllegalArgumentException("이미 닉네임이 존재하는 사용자입니다.");
        }

        // 새로운 닉네임 생성
        User user = User.builder()
                .kakaoId(kakaoId)
                .userNickname(newNickname)
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

    public String updateNickname(Long kakaoId, String newNickname) {
        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (newNickname == null || newNickname.trim().isEmpty()) {
            throw new IllegalArgumentException("유효한 닉네임을 입력하세요.");
        }

        boolean nicknameExists = userRepository.existsByNickname(newNickname);
        if (nicknameExists) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        user.setUserNickname(newNickname);
        userRepository.save(user);

        return "닉네임이 성공적으로 수정되었습니다.";
    }

    // ===========
//    @Transactional
//    public User saveOrUpdateUser(Long kakaoId, String nickname, String profileImageUrl) {
//        // 기존 사용자 확인
//        Optional<User> existingUser = userRepository.findByKakaoId(kakaoId);
//        if (existingUser.isPresent()) {
//            // 기존 사용자가 있다면 닉네임과 프로필 이미지를 업데이트
//            User user = existingUser.get();
//            user.setNickname(nickname); // 닉네임 업데이트 (필요한 경우)
//            user.setProfileImageUrl(profileImageUrl); // 프로필 이미지 업데이트
//            return userRepository.save(user);
//        } else {
//            // 새 사용자 생성
//            User newUser = User.builder()
//                    .kakaoId(kakaoId)
//                    .nickname(nickname)
//                    .profileImageUrl(profileImageUrl)
//                    .build();
//            return userRepository.save(newUser);
//        }
//    }

    @Transactional
    public Map<String, Object> saveOrUpdateUser(Long kakaoId, String nickname, String userNickname, String profileImageUrl) {
        Optional<User> existingUser = userRepository.findByKakaoId(kakaoId);

        if (existingUser.isPresent()) {
            // 기존 사용자가 존재하면 업데이트
            User user = existingUser.get();
            user.setNickname(nickname); // 카카오 닉네임 업데이트
            user.setProfileImageUrl(profileImageUrl); // 프로필 이미지 업데이트
            userRepository.save(user);

            return Map.of(
                    "status", "existing",
                    "message", "기존 회원 정보가 업데이트되었습니다.",
                    "user", user
            );
        } else {
            // 새 사용자를 생성
            User newUser = User.builder()
                    .kakaoId(kakaoId)
                    .nickname(nickname)
                    .userNickname(userNickname) // 처음 생성 시 userNickname을 빈 문자열로 설정
                    .profileImageUrl(profileImageUrl)
                    .build();
            User savedUser = userRepository.save(newUser);

            return Map.of(
                    "status", "new",
                    "message", "새로운 회원이 생성되었습니다.",
                    "user", savedUser
            );
        }
    }

}
