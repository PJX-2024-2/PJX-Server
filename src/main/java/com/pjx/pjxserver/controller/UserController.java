package com.pjx.pjxserver.controller;

import com.pjx.pjxserver.dto.UserProfileRequestDto;
import com.pjx.pjxserver.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

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

}
