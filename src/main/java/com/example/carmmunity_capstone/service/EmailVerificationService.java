package com.example.carmmunity_capstone.service;

import com.example.carmmunity_capstone.entity.EmailVerification;
import com.example.carmmunity_capstone.repository.EmailVerificationRepository;
import com.example.carmmunity_capstone.repository.UserRepository;
import com.example.carmmunity_capstone.util.EmailSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final EmailVerificationRepository repository;
    private final EmailSender emailSender; // 별도 구현 필요
    private final UserRepository userRepository;

    public void sendVerificationCode(String email, String purpose) {
        validatePurpose(purpose);

        if(purpose.equals("signup")){
            if (userRepository.findByEmail(email).isPresent()) {
                throw new IllegalArgumentException("이미 회원가입에 사용된 이메일입니다.");
            }
            String code = generateCode();

            EmailVerification verification = EmailVerification.builder()
                    .email(email)
                    .verificationCode(code)
                    .purpose(purpose)
                    .createdAt(LocalDateTime.now())
                    .isVerified(false)
                    .build();

            repository.save(verification);
            emailSender.send(email, "[CarCommunity] 회원가입용 인증 코드", "인증 코드는: " + code);
        } else if (purpose.equals("reset")) {
            String code = generateCode();

            EmailVerification verification = EmailVerification.builder()
                    .email(email)
                    .verificationCode(code)
                    .purpose(purpose)
                    .createdAt(LocalDateTime.now())
                    .isVerified(false)
                    .build();

            repository.save(verification);
            emailSender.send(email, "[CarCommunity] 비밀번호 초기화용 인증 코드", "인증 코드는: " + code);
        }
    }


    public void verifyAndMarkAsVerified(String email, String code, String purpose) {
        EmailVerification verification = repository
                .findTopByEmailAndPurposeOrderByCreatedAtDesc(email, purpose)
                .orElseThrow(() -> new IllegalArgumentException("인증 요청이 없습니다."));

        if (verification.isExpired()) {
            throw new IllegalArgumentException("인증 코드가 만료되었습니다.");
        }

        if (!verification.getVerificationCode().equals(code)) {
            throw new IllegalArgumentException("인증 코드가 일치하지 않습니다.");
        }

        verification.setIsVerified(true);
        repository.save(verification);

        if (verification.getIsVerified()) return;
    }

    private String generateCode() {
        return String.valueOf(new Random().nextInt(900000) + 100000); // 6자리 숫자
    }

    private void validatePurpose(String purpose) {
        if (!"signup".equals(purpose) && !"reset".equals(purpose)) {
            throw new IllegalArgumentException("지원하지 않는 인증 목적입니다.");
        }
    }
}
