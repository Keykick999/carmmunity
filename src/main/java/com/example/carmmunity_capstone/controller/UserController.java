package com.example.carmmunity_capstone.controller;

import com.example.carmmunity_capstone.jwt.JwtProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class UserController {

    private final JwtProvider jwtProvider;

    public UserController(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }


    // 테스트용 토큰 발급
    @PostMapping("/test-login")
    public ResponseEntity<String> testLogin() {
        String token = jwtProvider.createLongLivedToken("user_100", "user");
        return ResponseEntity.ok(token);
    }
}






