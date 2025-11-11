package com.example.carmmunity_capstone.config;

import com.example.carmmunity_capstone.entity.User;
import com.example.carmmunity_capstone.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;   // 변경점
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(
            JwtTokenProvider jwtTokenProvider,
            UserRepository userRepository,
            CustomUserDetailsService userDetailsService
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException, IOException {

        String token = resolveToken(request);

        if (token != null && jwtTokenProvider.validateToken(token)) {

            // 여기서 이제 UUID를 가져온다
            String userUuid = jwtTokenProvider.getUserId(token);

            // UUID로 유저 조회 (로그인 ID 아님)
            User user = userRepository.findByUserUuid(userUuid)
                    .orElse(null);

            if (user != null) {
                // 1. UserDetails는 권한 정보만 담는 용도로 사용합니다.
                // Principal 자리에 들어갈 UUID 문자열은 따로 준비합니다.
                String uuidForPrincipal = user.getUserUuid();

                UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                        .username(user.getUserUuid()) // UserDetails 내부 username
                        .password(user.getPassword())
                        .roles("USER")
                        .build();

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                // 2. Principal 자리에 user.getUserUuid() (String)을 직접 넣습니다.
                                uuidForPrincipal, // <--- 수정 포인트
                                null,
                                userDetails.getAuthorities()
                        );

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
