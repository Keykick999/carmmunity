package com.example.carmmunity_capstone.controller;

import com.example.carmmunity_capstone.config.JwtTokenProvider;
import com.example.carmmunity_capstone.dto.*;
import com.example.carmmunity_capstone.entity.EmailAlreadyInUseException;
import com.example.carmmunity_capstone.service.EmailVerificationService;
import com.example.carmmunity_capstone.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    // 회원가입
    @PostMapping
    public ResponseEntity<Void> register(@RequestBody @Valid UserJoinRequest request) {
        userService.register(request);
        return ResponseEntity.ok().build();
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<UserLoginResponse> login(@RequestBody @Valid UserLoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    // 회원 정보 조회
    @GetMapping("/info")
    public ResponseEntity<UserResponse> getMyInfo(@RequestHeader("Authorization") String token) {
        String userUuId = jwtTokenProvider.getUserId(token.replace("Bearer ", ""));
        return ResponseEntity.ok(userService.getUserInfo(userUuId));
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

    //비밀번호 초기화
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        userService.resetPassword(
                request.getEmail(),
                request.getNewPassword(),
                request.getVerificationCode()
        );
        return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
    }

    // =======================================================
    // 소셜 로그인 이메일 미제공 시 최종 회원가입 API
    // POST /users/social-complete
    // =======================================================
    @PostMapping("/social-complete")
    public ResponseEntity<Map<String, String>> completeSocialRegistration(@RequestBody @Valid SocialCompleteRequest request) {
        System.out.println("request = " + request);
        try {
            userService.completeSocialRegistration(request);

            // 성공 시, 201 Created 응답 (프론트에서 이 후 /login으로 리다이렉트 유도)
            return new ResponseEntity<>(
                    Collections.singletonMap("message", "회원가입이 완료되었습니다."),
                    HttpStatus.CREATED);
        } catch (EmailAlreadyInUseException e) {
            // 이메일 중복 오류 처리 (400 Bad Request)
            System.out.println("이메일 중복");
            return new ResponseEntity<>(
                    Collections.singletonMap("message", e.getMessage()),
                    HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            // 기타 오류 처리 (500 Internal Server Error)
            return new ResponseEntity<>(
                    Collections.singletonMap("message", "회원가입 처리 중 알 수 없는 오류가 발생했습니다."),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * [신규] 계정 연동 확인 후 최종적으로 외부 Provider 정보를 DB에 링크하는 API
     * POST /users/link-account
     */
    @PostMapping("/link-account")
    public ResponseEntity<UserLinkResponse> linkAccount(@RequestBody @Valid UserLinkRequest request) {
        UserLinkResponse response = userService.linkAccount(request);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
}
