package com.pjx.pjxserver.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class LoginController {

    @GetMapping({"/", ""})
    public String login() {
        return "Please login using Kakao";
    }

    @GetMapping("/loginSuccess")
    public String loginSuccess(@AuthenticationPrincipal OAuth2User oauth2User) {
        Map<String, Object> attributes = oauth2User.getAttributes();
        // 카카오톡에서 제공하는 닉네임을 가져옴
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
        String nickname = (String) profile.get("nickname");

        return "Hello, " + nickname + "!";
    }
}
