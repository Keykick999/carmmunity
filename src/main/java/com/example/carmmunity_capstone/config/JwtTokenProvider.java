package com.example.carmmunity_capstone.config;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${spring.jwt.secret}")
    private String secretKey;

    @Value("${spring.jwt.access-exp-ms}")
    private Date accessExpTime;

    @Value("${spring.jwt.refresh-exp-ms}")
    private Date refreshExpTime;

    private SecretKey key;

    //시크릿 키 초기화
    @PostConstruct
    protected void init(@Value("${spring.jwt.secret}") String secretKey,
                        @Value("${spring.jwt.access-exp-ms}") long accessExpMs,
                        @Value("${spring.jw.refresh-exp-ms}") long refreshExpMs) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
        this.accessExpTime = new Date(System.currentTimeMillis() + accessExpMs);
        this.refreshExpTime = new Date(System.currentTimeMillis() + refreshExpMs);
    }

    // JWT 토큰 생성
    public String createAccessToken(String userId) {
        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(accessExpTime)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken(String userId) {
        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(refreshExpTime)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // JWT에서 사용자 ID 추출
    public String getUserId(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // JWT 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}