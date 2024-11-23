package com.pjx.pjxserver.controller;

import com.pjx.pjxserver.common.JwtUtil;
import com.pjx.pjxserver.dto.UserProfileRequestDto;
import com.pjx.pjxserver.service.UserService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "사용자", description = "사용자 관련 API")
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


    @Operation(
            summary = "프로필 이미지 조회",
            description = "사용자의 프로필 이미지를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "프로필 이미지 URL 반환",
                            content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"))),
                    @ApiResponse(responseCode = "400", description = "유효하지 않은 요청",
                            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{}")))
            }
    )
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

    @Operation(
            summary = "프로필 이미지 업로드",
            description = "사용자의 프로필 이미지를 업로드합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "업로드 성공",
                            content = @Content(mediaType = "application/json", schema = @Schema(type = "string"))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청",
                            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{}")))
            }
    )
    @PostMapping(value = "/profile/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> uploadProfile(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("profileImage") MultipartFile profileImage) throws IOException {

        Long kakaoId = extractKakaoIdFromJwt(authHeader);
        UserProfileRequestDto requestDto = new UserProfileRequestDto(kakaoId, profileImage);
        String profileImageUrl = userService.uploadProfileImage(requestDto);
        return ResponseEntity.ok(profileImageUrl);
    }

    @Operation(
            summary = "프로필 이미지 삭제",
            description = "사용자의 프로필 이미지를 삭제합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "삭제 성공",
                            content = @Content(mediaType = "application/json", schema = @Schema(type = "string"))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청",
                            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{}")))
            }
    )
    @DeleteMapping("/profile/delete")
    public ResponseEntity<String> deleteProfile(@RequestHeader("Authorization") String authHeader) {
        Long kakaoId = extractKakaoIdFromJwt(authHeader);

        userService.deleteProfileImage(kakaoId);
        return ResponseEntity.ok("프로필 삭제 완료!\n");
    }


    @Operation(
            summary = "친구 팔로우 추가",
            description = "친구 닉네임을 통해 팔로우를 추가합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "팔로우 성공",
                            content = @Content(mediaType = "application/json",
                                    examples = @ExampleObject(value = """
                            {
                                "message": "팔로우 성공"
                            }
                            """))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청",
                            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{}")))
            }
    )
    @PostMapping("/{friendUserNickname}/follow")
    public ResponseEntity<Map<String, String>> followUser(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String friendUserNickname) {

        Long userKakaoId = extractKakaoIdFromJwt(authHeader);
        Map<String, String> response = new HashMap<>();
        try {
            String message = userService.followUserByUserNickname(userKakaoId, friendUserNickname);
            response.put("message", message);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }


    @Operation(
            summary = "친구 팔로우 취소",
            description = "친구 닉네임을 통해 팔로우를 취소합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "팔로우 취소 성공",
                            content = @Content(mediaType = "application/json",
                                    examples = @ExampleObject(value = """
                            {
                                "message": "팔로우 취소 성공"
                            }
                            """))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청",
                            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{}")))
            }
    )
    @DeleteMapping("/{friendUserNickname}/follow")
    public ResponseEntity<Map<String, String>> unfollowUser(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String friendUserNickname) {

        Long userKakaoId = extractKakaoIdFromJwt(authHeader);
        Map<String, String> response = new HashMap<>();
        try {
            String message = userService.unfollowUserByUserNickname(userKakaoId, friendUserNickname);
            response.put("message", message);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @Operation(
            summary = "팔로우 여부 확인",
            description = "특정 사용자를 팔로우하고 있는지 여부를 확인합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "팔로우 여부 확인 성공",
                            content = @Content(mediaType = "application/json",
                                    examples = @ExampleObject(value = """
                            {
                                "isFollowing": true
                            }
                            """)))
            }
    )
    @GetMapping("/{friendUserNickname}/is-following")
    public ResponseEntity<Map<String, Boolean>> isFollowing(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String friendUserNickname) {

        Long userKakaoId = extractKakaoIdFromJwt(authHeader);
        boolean isFollowing = userService.isFollowingByUserNickname(userKakaoId, friendUserNickname);

        Map<String, Boolean> response = new HashMap<>();
        response.put("isFollowing", isFollowing);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "마이페이지에서 닉네임 수정",
            description = "사용자의 닉네임을 수정합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "닉네임 수정 성공",
                            content = @Content(mediaType = "application/json",
                                    examples = @ExampleObject(value = """
                            {
                                "status": 200,
                                "message": "닉네임 수정 완료",
                                "newNickname": "새로운닉네임"
                            }
                            """))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청",
                            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{}")))
            }
    )
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

}
