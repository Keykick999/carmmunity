package com.example.carmmunity_capstone.service;

import com.example.carmmunity_capstone.domain.User;
import com.example.carmmunity_capstone.exception.InvalidTokenException;
import com.example.carmmunity_capstone.jwt.JwtProvider;
import com.example.carmmunity_capstone.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    public UserService(JwtProvider jwtProvider, UserRepository userRepository) {
        this.jwtProvider = jwtProvider;
        this.userRepository = userRepository;
    }


    // 회원 유효성 검사
    public User validateUser(String authorizationHeader) {
        String userId = jwtProvider.extractUserId(authorizationHeader);
        return userRepository.findById(userId).orElseThrow(() -> new InvalidTokenException("유효하지 않은 토큰입니다."));
    }

    public Optional<User> getUserIfAuthenticated(String authorizationHeader) {
        try {
            if (authorizationHeader == null || authorizationHeader.isBlank()) {
                return Optional.empty();
            }

            String userId = jwtProvider.extractUserId(authorizationHeader);
            return userRepository.findById(userId);
        } catch (Exception e) {
            return Optional.empty();    // 토큰 없거나 만료되면 그냥 비인증 처리
        }
    }

    // 회원 id로 조회
    public Optional<User> findById(String userId) {
        return userRepository.findById(userId);
    }
}
