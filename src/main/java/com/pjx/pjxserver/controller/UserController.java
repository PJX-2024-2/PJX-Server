package com.pjx.pjxserver.controller;

import com.pjx.pjxserver.domain.User;
import com.pjx.pjxserver.dto.UserCreateDTO;
import com.pjx.pjxserver.dto.UserDTO;
import com.pjx.pjxserver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserService userService;

    // 사용자 ID로 사용자 정보 조회
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        UserDTO userDTO = new UserDTO();
        userDTO.setKakaoLoginId(user.getKakaoLoginId());
        userDTO.setEmail(user.getEmail());
        return ResponseEntity.ok(userDTO);
    }
//
//    // 사용자 생성 (회원가입)
//    @PostMapping("/users")
//    public ResponseEntity<UserDTO> createUser(@RequestBody User user) {
//        User createdUser = userService.createUser(user);
//        UserDTO userDTO = new UserDTO();
//        userDTO.setKakaoLoginId(createdUser.getKakaoLoginId());
//        userDTO.setEmail(createdUser.getEmail());
//        return ResponseEntity.status(HttpStatus.CREATED).body(userDTO);
//    }
@PostMapping("/users")
public ResponseEntity<UserDTO> createUser(@RequestBody UserCreateDTO userCreateDTO) {
    User user = new User();
    user.setEmail(userCreateDTO.getEmail());
    user.setPassword(userCreateDTO.getPassword());
    user.setNickname(userCreateDTO.getNickname());
    user.setGender(userCreateDTO.getGender());
    user.setBirthdate(userCreateDTO.getBirthdate());
    user.setAnnualIncome(userCreateDTO.getAnnualIncome());
    user.setKakaoLoginId(userCreateDTO.getKakaoLoginId());

    User createdUser = userService.createUser(user);
    UserDTO userDTO = new UserDTO();
    userDTO.setKakaoLoginId(createdUser.getKakaoLoginId());
    userDTO.setEmail(createdUser.getEmail());
    return ResponseEntity.status(HttpStatus.CREATED).body(userDTO);
}

    // 카카오 로그인 처리
    @PostMapping("/login")
    public ResponseEntity<UserDTO> loginWithKakao(@RequestBody String kakaoToken) {
        User user = userService.loginWithKakao(kakaoToken);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserDTO userDTO = new UserDTO();
        userDTO.setKakaoLoginId(user.getKakaoLoginId());
        userDTO.setEmail(user.getEmail());
        return ResponseEntity.ok(userDTO);
    }
}


//@RestController
//@RequestMapping("/api/users")
//public class UserController {
//
//    @Autowired
//    private UserService userService;
//
//    @PostMapping("/login/kakao")
//    public ResponseEntity<User> kakaoLogin(@RequestParam String kakaoToken) {
//        // 카카오 토큰을 이용해 사용자 정보를 가져오고, DB에 저장 후 반환
////        User user = userService.loginWithKakao(kakaoToken);
////        return ResponseEntity.ok(user);
//    }
//
//    @GetMapping("/users/me")
//    public ResponseEntity<User> getMyInfo() {
//        // 인증된 사용자의 정보를 반환 (카카오 로그인 후 세션 기반 또는 JWT 사용)
////        User currentUser = userService.getAuthenticatedUser();
////        return ResponseEntity.ok(currentUser);
//    }
//}
//
