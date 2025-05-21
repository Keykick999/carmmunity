package com.example.carmmunity_capstone.service;


import com.example.carmmunity_capstone.config.JwtTokenProvider;
import com.example.carmmunity_capstone.dto.*;
import com.example.carmmunity_capstone.entity.User;
import com.example.carmmunity_capstone.exception.UnauthenticatedUserException;
import com.example.carmmunity_capstone.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final EmailVerificationService emailVerificationService;

    // 회원가입 처리
    @Transactional
    public void register(UserJoinRequest request) {
        if (userRepository.existsById(request.getUserId())) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }

        // ✅ 이메일 인증 코드 검증
        emailVerificationService.verifyAndMarkAsVerified(
                request.getEmail(),
                request.getVerificationCode(),
                "signup"
        );

        User user = User.builder()
                .userId(request.getUserId())
                .nickname(request.getNickname())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .phone(request.getPhone())
                .name(request.getName())
                .introduction("자기소개가 아직 없습니다.")
                .profileImagePath("/image/UserImageDefault.png")
                .build();

        userRepository.save(user);
    }

    // 로그인 처리
    public UserLoginResponse login(UserLoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUserId(), request.getPassword())
            );
        } catch (Exception e) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        String token = jwtTokenProvider.createToken(request.getUserId());
        return new UserLoginResponse(token);
    }


    // 회원 정보 조회
    public UserResponse getUserInfo(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return new UserResponse(
                user.getUserId(),
                user.getNickname(),
                user.getEmail(),
                user.getPhone(),
                user.getName(),
                user.getCreatedAt(),
                user.getIntroduction(),
                user.getProfileImagePath()
        );
    }

    // 회원 정보 수정
    @Transactional
    public void updateUserInfo(String userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        user.setNickname(request.getNickname());
        user.setPhone(request.getPhone());

        // ✅ 추가된 필드 처리
        user.setIntroduction(request.getIntroduction());
        user.setProfileImagePath(request.getProfileImagePath());

        userRepository.save(user);


    }

    //회원 정보 수정 - 유저 업로드 이미지 기준
    @Transactional // ✅ 트랜잭션 보장
    public void updateUserProfileWithImage(String userId, String nickname, String phone, String introduction,
                                           MultipartFile profileImage, String defaultImage) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        user.setNickname(nickname);
        user.setPhone(phone);
        user.setIntroduction(introduction);

        String imagePath = saveImage(profileImage);

        if (imagePath != null) {
            user.setProfileImagePath(imagePath); // 사용자 이미지 경로 저장
        } else if (defaultImage != null) {
            user.setProfileImagePath(defaultImage); // 기본 이미지 저장
        }

        userRepository.save(user);
    }

    // 이미지 저장
    public String saveImage(MultipartFile imageFile) {
        // 1. 저장할 실제 경로
        Path path = Paths.get("C:", "uploads"); // 경로 변경: "C:/uploads"
        String uploadDir = path.toString();

        String storedFileName = null;

        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                // 2. 파일명 처리
                String originalFileName = imageFile.getOriginalFilename();
                String ext = originalFileName.substring(originalFileName.lastIndexOf("."));
                storedFileName = UUID.randomUUID().toString() + ext;

                // 3. 실제 저장
                File dest = new File(uploadDir, storedFileName);
                if (!dest.getParentFile().exists()) {
                    dest.getParentFile().mkdirs();
                }
                imageFile.transferTo(dest);


                // ✅ 4. DB에는 상대 경로로 저장
                return "/uploads/" + storedFileName;

            } catch (IOException e) {
                throw new RuntimeException("이미지 저장 중 오류 발생", e);
            }
        }

        return null;
    }


    // 아이디 찾기
    public String findIdByEmail(String email) {
        return userRepository.findIdByEmail(email)
                .map(User::getUserId)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일로 가입된 계정이 없습니다."));
    }




    // 요청보낸 회원의 id값 확인
    public String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();  // JWT에서 setSubject(userId)로 설정했던 값
    }

    public User getAuthenticatedUser() {
        String userId = getCurrentUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new UnauthenticatedUserException("인증된 사용자가 아닙니다."));
    }

    public Optional<User> getUserIfAuthenticated() {
        String userId = getCurrentUserId();
        return userRepository.findById(userId);
    }

    public Optional<User> findById(String userId) {
        return userRepository.findById(userId);
    }



    /*
    // 비밀번호 재설정
    @Transactional
    public void resetPassword(PasswordResetRequest request) {
        emailVerificationService.verifyAndMarkAsVerified(request.getEmail(), "reset");

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("이메일에 해당하는 사용자가 없습니다."));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
    */

    /*
    // 회원 탈퇴
    @Transactional
    public void withdraw(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        WithdrawnUser withdrawnUser = WithdrawnUser.builder()
                .userId(user.getUserId())
                .deletedAt(LocalDateTime.now())
                .build();

        withdrawnUserRepository.save(withdrawnUser);
        userRepository.delete(user);
    }
     */
}

