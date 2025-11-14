package com.example.carmmunity_capstone.config;

import com.example.carmmunity_capstone.dto.OAuthAttributes;
import com.example.carmmunity_capstone.entity.User;
import com.example.carmmunity_capstone.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    private final String FRONTEND_CALLBACK_URL = "http://localhost:8080/oauth/callback";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        // 1. 인증 객체에서 데이터 추출
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;
        String registrationId = authToken.getAuthorizedClientRegistrationId();

        // 2. OAuthAttributes DTO로 파싱 (이메일을 얻기 위함)
        OAuthAttributes attributes = OAuthAttributes.of(
                registrationId,
                oAuth2User.getName(),
                oAuth2User.getAttributes()
        );

        // 3. attributes DTO에서 이메일 추출 (SuccessHandler에 도달했다면 이메일은 null이 아님)
        String email = attributes.getEmail();

        // 4. 이메일로 DB에서 User 조회 (CustomOAuth2UserService가 저장을 완료했으므로 반드시 존재)
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("FATAL: User not found after successful save in UserService.")); // 심각한 오류로 처리

        // 5. JWT 토큰 생성 및 발행 (userUuid 사용)
        String accessToken = jwtTokenProvider.createToken(user.getUserUuid());

        // 6. 프론트엔드로 리다이렉트
        String targetUrl = UriComponentsBuilder.fromUriString(FRONTEND_CALLBACK_URL)
                .queryParam("accessToken", accessToken)
                .build()
                .encode(StandardCharsets.UTF_8)
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}