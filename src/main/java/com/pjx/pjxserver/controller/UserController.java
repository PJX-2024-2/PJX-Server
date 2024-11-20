package com.pjx.pjxserver.controller;

import com.pjx.pjxserver.common.JwtUtil;
import com.pjx.pjxserver.dto.UserProfileRequestDto;
import com.pjx.pjxserver.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import com.pjx.pjxserver.domain.Spending;
import com.pjx.pjxserver.domain.User;
import com.pjx.pjxserver.service.SpendingService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private final UserService userService;
    @Autowired
    private final SpendingService spendingService;

    // JWT에서 kakaoId를 추출하는 공통 메서드
    private Long extractKakaoIdFromJwt(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7); // "Bearer " 이후의 토큰만 추출
        return Long.valueOf(jwtUtil.extractSubject(token)); // JWT의 subject에서 kakaoId 추출
    }

    @GetMapping("/profile")
    public ResponseEntity<String> getProfileImage(@RequestHeader("Authorization") String authHeader) {
        Long kakaoId = extractKakaoIdFromJwt(authHeader);

        String profileImageUrl = userService.getProfileImageUrl(kakaoId);
        if (profileImageUrl == null) {
            // 프로필 이미지가 없는 경우
            return ResponseEntity.ok("프로필 이미지가 없습니다.");
        }
        return ResponseEntity.ok(profileImageUrl);
    }

    @PostMapping(value = "/profile/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> uploadProfile(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("profileImage") MultipartFile profileImage) throws IOException {

        Long kakaoId = extractKakaoIdFromJwt(authHeader);
        UserProfileRequestDto requestDto = new UserProfileRequestDto(kakaoId, profileImage);
        String profileImageUrl = userService.uploadProfileImage(requestDto);
        return ResponseEntity.ok(profileImageUrl);
    }

    @DeleteMapping("/profile/delete")
    public ResponseEntity<String> deleteProfile(@RequestHeader("Authorization") String authHeader) {
        Long kakaoId = extractKakaoIdFromJwt(authHeader);

        userService.deleteProfileImage(kakaoId);
        return ResponseEntity.ok("프로필 삭제 완료!\n");
    }

    @Operation(summary = "피드에서 닉네임으로 친구를 검색하는 api입니다", description = "닉네임으로 친구를 검색합니다.")
    @GetMapping("/feed/search")
    public ResponseEntity<Map<String, Object>> searchUsers(@RequestParam String nickname) {
        List<User> users = userService.searchUsersByNickname(nickname);
        Map<String, Object> response = new HashMap<>();

        if (users.isEmpty()) {
            response.put("status", HttpStatus.NOT_FOUND.value());
            response.put("message", "해당 닉네임과 동일한 친구를 찾지 못했습니다."); // "No users found with the given nickname."
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        response.put("status", HttpStatus.OK.value());
        response.put("message", "친구 목록을 성공적으로 조회했습니다."); // "Successfully retrieved friend list."
        response.put("data", users);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "피드에서 닉네임으로 친구를 검색 후 친구를 추가하는 api 입니다", description = "사용자가 친구를 추가합니다.")
    @PostMapping("/feed/add-friend")
    public ResponseEntity<Map<String, Object>> addFriendByKakaoId(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) String friendNickname,
            @RequestParam(required = false) Long friendKakaoId) {

        Long kakaoId = extractKakaoIdFromJwt(authHeader);

        Map<String, Object> response = new HashMap<>();
        try {
            String responseMessage = userService.addFriendByKakaoId(kakaoId, friendNickname, friendKakaoId);
            response.put("status", HttpStatus.OK.value());
            response.put("message", responseMessage);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("status", HttpStatus.BAD_REQUEST.value());
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @Operation(summary = "내 화면인지, 친구 화면인지 구분할 수 있는 kakaoId로 친구를 피드에서 클릭했을때 확인해주는 api", description = "피드가 자신의 피드인지 확인합니다.")
    @GetMapping("/feed/check")
    public ResponseEntity<Map<String, Object>> checkFeedOwner(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam Long targetKakaoId) {

        Long userKakaoId = extractKakaoIdFromJwt(authHeader);
        boolean isOwnFeed = userService.isOwnFeed(userKakaoId, targetKakaoId);

        Map<String, Object> response = new HashMap<>();
        if (isOwnFeed) {
            response.put("status", HttpStatus.OK.value());
            response.put("message", "본인의 피드입니다.");
        } else {
            response.put("status", HttpStatus.OK.value());
            response.put("message", "친구의 피드입니다.");
        }

        response.put("isOwnFeed", isOwnFeed);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "친구의 카카오 아이디로 팔로우 추가하는 api 입니다")
    @PostMapping("/{friendKakaoId}/follow")
    public ResponseEntity<Map<String, String>> followUser(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long friendKakaoId) {

        Long userKakaoId = extractKakaoIdFromJwt(authHeader);
        Map<String, String> response = new HashMap<>();
        try {
            String message = userService.followUser(userKakaoId, friendKakaoId);
            response.put("message", message);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @Operation(summary = "친구의 카카오 아이디로 팔로우 취소하는 api입니다")
    @DeleteMapping("/{friendKakaoId}/follow")
    public ResponseEntity<Map<String, String>> unfollowUser(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long friendKakaoId) {

        Long userKakaoId = extractKakaoIdFromJwt(authHeader);
        Map<String, String> response = new HashMap<>();
        try {
            String message = userService.unfollowUser(userKakaoId, friendKakaoId);
            response.put("message", message);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @Operation(summary = "팔로우중인지 아닌지 상태를 확인하는 api")
    @GetMapping("/{friendKakaoId}/is-following")
    public ResponseEntity<Map<String, Boolean>> isFollowing(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long friendKakaoId) {

        Long userKakaoId = extractKakaoIdFromJwt(authHeader);
        boolean isFollowing = userService.isFollowing(userKakaoId, friendKakaoId);

        Map<String, Boolean> response = new HashMap<>();
        response.put("isFollowing", isFollowing);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "마이페이지에서 닉네임을 수정하는 API입니다", description = "사용자의 닉네임을 수정합니다.")
    @PatchMapping("/nickname")
    public ResponseEntity<Map<String, Object>> updateNickname(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String newNickname) {

        Long kakaoId = extractKakaoIdFromJwt(authHeader);
        Map<String, Object> response = new HashMap<>();
        try {
            String message = userService.updateNickname(kakaoId, newNickname);
            response.put("status", HttpStatus.OK.value());
            response.put("message", message);
            response.put("newNickname", newNickname);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("status", HttpStatus.BAD_REQUEST.value());
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}
