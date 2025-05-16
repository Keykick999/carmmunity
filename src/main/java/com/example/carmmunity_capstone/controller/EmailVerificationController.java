package com.example.carmmunity_capstone.controller;

import com.example.carmmunity_capstone.dto.EmailCodeSendRequest;
import com.example.carmmunity_capstone.dto.EmailCodeVerifyRequest;
import com.example.carmmunity_capstone.service.EmailVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/email")
@RequiredArgsConstructor
public class EmailVerificationController {

    private final EmailVerificationService service;

    @PostMapping("/send")
    public ResponseEntity<?> sendCode(@RequestBody @Valid EmailCodeSendRequest request) {
        System.out.println("request = " + request);
        service.sendVerificationCode(
                request.getEmail(),
                request.getPurpose()
        );
        return ResponseEntity.ok("인증 코드 발송 완료");
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyCode(@RequestBody @Valid EmailCodeVerifyRequest request) {
        service.verifyAndMarkAsVerified(
                request.getEmail(),
                request.getCode(),
                request.getPurpose()
        );
        return ResponseEntity.ok("인증 성공");
    }
}
