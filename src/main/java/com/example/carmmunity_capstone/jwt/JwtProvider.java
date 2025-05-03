package com.example.carmmunity_capstone.jwt;

import com.example.carmmunity_capstone.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Date;

@Component
public class JwtProvider {

    private final JwtConfig jwtConfig;
    private String encodedSecret;

    public JwtProvider(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    @PostConstruct
    protected void init() {
        encodedSecret = Base64.getEncoder().encodeToString(jwtConfig.getSecretKey().getBytes());
    }

    // 토큰 생성
    public String createToken(String userId, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtConfig.getExpirationTime());

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(SignatureAlgorithm.HS256, encodedSecret)
                .compact();
    }

    // 테스트용 - 30일 유효한 토큰 생성
    public String createLongLivedToken(String userId, String role) {
        Date now = new Date();
        long thirtyDaysInMillis = 1000L * 60 * 60 * 24 * 30; // 30일
        Date expiry = new Date(now.getTime() + thirtyDaysInMillis);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(SignatureAlgorithm.HS256, encodedSecret)
                .compact();
    }



    // userId 추출
    public String extractUserId(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(encodedSecret)
                    .parseClaimsJws(removeBearerPrefix(token))
                    .getBody();

            String userId = String.valueOf(claims.getSubject());
            return userId;
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("JWT 토큰이 만료되었습니다.");
        } catch (Exception e) {
            throw new RuntimeException("유효하지 않은 JWT 토큰입니다.");
        }
    }

    // Bearer 접두사 제거
    private String removeBearerPrefix(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return token;
    }
}
