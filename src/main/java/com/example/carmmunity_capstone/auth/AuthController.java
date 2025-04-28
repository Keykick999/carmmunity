package com.example.carmmunity_capstone.auth;

import com.example.carmmunity_capstone.jwt.JwtProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtProvider jwtProvider;

    public AuthController(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @GetMapping("/token")
    public ResponseEntity<String> generateToken() {
        String userId = "366";
        String role = "user";
        return ResponseEntity.ok(jwtProvider.createToken(userId, role));
    }

    @GetMapping("/getUserId")
    public ResponseEntity<String> getUserId() {
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIzNjYiLCJyb2xlIjoidXNlciIsImlhdCI6MTc0NDQ1MDkwNSwiZXhwIjoxNzQ0NDU0NTA1fQ.Jwgf2CKfW-oAJXASOEPq_RrwoBPbn6u2EXlnwtAI_E0";
        String userId = jwtProvider.extractUserId(token);
        return ResponseEntity.ok(userId);
    }
}
