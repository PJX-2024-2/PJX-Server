package com.pjx.pjxserver.controller;

import com.pjx.pjxserver.dto.UserProfileRequestDto;
import com.pjx.pjxserver.service.UserService;
import lombok.RequiredArgsConstructor;
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

    private final UserService userService;
    private final SpendingService spendingService;

    @GetMapping("/profile/{kakaoId}")
    public ResponseEntity<String> getProfileImage(@PathVariable Long kakaoId) {
        String profileImageUrl = userService.getProfileImageUrl(kakaoId);
        if (profileImageUrl == null) {
            // 프로필 이미지가 없는 경우 404 Not Found가 아니라 프로필이미지가 없습니다로 대체
            return ResponseEntity.ok("프로필 이미지가 없습니다.");
        }
        return ResponseEntity.ok(profileImageUrl);
    }

    @PostMapping(value = "/profile/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> uploadProfile(@RequestParam("kakaoId") Long kakaoId,
                                                @RequestParam("profileImage") MultipartFile profileImage) throws IOException {
        UserProfileRequestDto requestDto = new UserProfileRequestDto(kakaoId, profileImage);
        String profileImageUrl = userService.uploadProfileImage(requestDto);
        return ResponseEntity.ok(profileImageUrl);
    }
    
    @DeleteMapping("/profile/delete/{kakaoId}")
    public ResponseEntity<String> deleteProfile(@PathVariable Long kakaoId) {
        userService.deleteProfileImage(kakaoId);
        return ResponseEntity.ok("프로필 삭제 완료!\n");
    }
    
    // ============================
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
            @RequestParam Long kakaoId,
            @RequestParam(required = false) String friendNickname,
            @RequestParam(required = false) Long friendKakaoId) {

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
            @RequestParam Long userKakaoId,
            @RequestParam Long targetKakaoId) {

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
            @RequestParam Long userKakaoId,
            @PathVariable Long friendKakaoId) {
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
            @RequestParam Long userKakaoId,
            @PathVariable Long friendKakaoId) {
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
            @RequestParam Long userKakaoId,
            @PathVariable Long friendKakaoId) {
        boolean isFollowing = userService.isFollowing(userKakaoId, friendKakaoId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("isFollowing", isFollowing);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "피드 - 팔로우한 친구들의 지출 내역을 요약 조회하는 api입니다", description = "팔로우한 친구의 지출 내역 중 description, amount, kakaoId만 조회합니다.")
    @GetMapping("/feed/friends/expenses")
    public ResponseEntity<Map<String, Object>> getFriendSpendingSummary(@RequestParam Long kakaoId) {
        // 팔로우한 친구의 지출 요약 목록을 조회
        List<Map<String, Object>> spendingSummaryList = spendingService.getFriendSpendingSummary(kakaoId);

        Map<String, Object> response = new HashMap<>();
        response.put("data", spendingSummaryList);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "피드에서 친구의 지출내역에 대한 리액션을 등록하는 api")
    @PostMapping("/spendings/{spendingId}/reactions")
    public ResponseEntity<Map<String, Object>> addReaction(
            @PathVariable Long spendingId,
            @RequestParam Long kakaoId,
            @RequestParam String reactionType) {

        spendingService.addReaction(spendingId, kakaoId, reactionType);

        // 응답 메시지를 Map 형식으로 구성
        Map<String, Object> response = new HashMap<>();
        response.put("message", "리액션이 성공적으로 추가되었습니다");

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "홈4 - 특정 날짜의 지출에 대한 리액션을 추가하는 POST method api입니다")
    @PostMapping("/submit-reaction")
    public ResponseEntity<Map<String, Object>> submitReaction(
            @RequestParam Long kakaoId,
            @RequestParam LocalDate date,
            @RequestParam String reactionType) {

        // SpendingService를 호출하여 감정을 리액션으로 저장
        spendingService.submitReaction(kakaoId, date, reactionType);

        // 응답 메시지를 Map 형식으로 구성
        Map<String, Object> response = new HashMap<>();
        response.put("message", "오늘의 리액션이 성공적으로 추가되었습니다");
        response.put("date", date);
        response.put("reaction", reactionType);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "홈5 - 특정 월의 모든 지출 내역에 대한 감정 리스트 조회하는 POST method api", description = "해당 월의 지출 내역에 대한 리액션 목록을 반환합니다.")
    @PostMapping("/reactions/by-month")
    public ResponseEntity<Map<String, Object>> getReactionsByMonth(
            @RequestParam Long kakaoId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") String month) {

        LocalDate startDate = LocalDate.parse(month + "-01");
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<Spending> spendingList = spendingService.getSpendingListByDateRange(kakaoId, startDate, endDate);

        List<Map<String, Object>> reactionsList = spendingList.stream()
                .map(spending -> {
                    Map<String, Object> spendingData = new HashMap<>();
                    spendingData.put("date", spending.getDate());
                    spendingData.put("reactions", spending.getReactions());
                    return spendingData;
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("month", month);
        response.put("reactionsList", reactionsList);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "마이페이지에서 닉네임을 수정하는 API입니다", description = "사용자의 닉네임을 수정합니다.")
    @PatchMapping("/{kakaoId}/nickname")
    public ResponseEntity<Map<String, Object>> updateNickname(
            @PathVariable Long kakaoId,
            @RequestParam String newNickname) {

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
