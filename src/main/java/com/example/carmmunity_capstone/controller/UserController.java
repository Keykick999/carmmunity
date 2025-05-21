package com.example.carmmunity_capstone.controller;

import com.example.carmmunity_capstone.config.JwtTokenProvider;
import com.example.carmmunity_capstone.dto.*;
import com.example.carmmunity_capstone.service.EmailVerificationService;
import com.example.carmmunity_capstone.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailVerificationService emailVerificationService;

    // 회원가입
    @PostMapping
    public ResponseEntity<Void> register(@RequestBody @Valid UserJoinRequest request) {
        userService.register(request);
        return ResponseEntity.ok().build();
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<UserLoginResponse> login(@RequestBody @Valid UserLoginRequest request) {
        System.out.println("request = " + request);
        return ResponseEntity.ok(userService.login(request));
    }

    // 회원 정보 조회
    @GetMapping("/info")
    public ResponseEntity<UserResponse> getMyInfo(@RequestHeader("Authorization") String token) {
        String userId = jwtTokenProvider.getUserId(token.replace("Bearer ", ""));
        return ResponseEntity.ok(userService.getUserInfo(userId));
    }

    // 회원 정보 수정 - 기본 이미지 이용 시
    @PutMapping("/info")
    public ResponseEntity<Void> updateMyInfo(@RequestHeader("Authorization") String token,
                                             @RequestBody @Valid UserUpdateRequest request) {
        String userId = jwtTokenProvider.getUserId(token.replace("Bearer ", ""));
        userService.updateUserInfo(userId, request);
        return ResponseEntity.ok().build();
    }

    //회원 정보 수정 -사용자가 원하는 이미지 삽입 시
    @PostMapping("/update-profile")
    public ResponseEntity<Void> updateProfile(
            @RequestHeader("Authorization") String token,
            @RequestParam("nickname") String nickname,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "introduction", required = false) String introduction,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
            @RequestParam(value = "defaultImage", required = false) String defaultImage
    ) {
        String userId = jwtTokenProvider.getUserId(token.replace("Bearer ", ""));
        userService.updateUserProfileWithImage(userId, nickname, phone, introduction, profileImage, defaultImage);
        return ResponseEntity.ok().build();
    }



    // 아이디 찾기
    @PostMapping("/find-id")
    public ResponseEntity<Map<String, String>> findUserId(@RequestBody @Valid FindIdRequest request) {
        String userId = userService.findIdByEmail(request.getEmail());
        return ResponseEntity.ok(Map.of("userId", userId));
    }



    /*
    // 비밀번호 재설정 요청 - 인증 코드 전송만 수행
    @PostMapping("/help/pw")
    public ResponseEntity<String> requestPasswordReset(@RequestParam String email) {
        emailVerificationService.sendVerificationCode(email, "reset");
        return ResponseEntity.ok("비밀번호 재설정 코드 전송 완료");
    }
     */

    /*
    // 비밀번호 재설정 실행 (코드 검증 포함)
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody @Valid PasswordResetRequest request) {
        userService.resetPassword(request);
        return ResponseEntity.ok().build();
    }
    */

    /*
    // 회원 탈퇴
    @DeleteMapping("/withdraw")
    public ResponseEntity<Void> withdraw(@RequestHeader("Authorization") String token) {
        String userId = jwtTokenProvider.getUserIdFromToken(token.replace("Bearer ", ""));
        userService.withdraw(userId);
        return ResponseEntity.ok().build();
    }
    */
}
