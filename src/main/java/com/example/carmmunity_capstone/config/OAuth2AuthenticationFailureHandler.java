package com.example.carmmunity_capstone.config;

// ... (import 구문 유지) ...
import com.example.carmmunity_capstone.dto.OAuth2FailureData;
import com.example.carmmunity_capstone.exception.AdditionalInfoRequiredException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final String FRONTEND_ERROR_CALLBACK_URL = "http://localhost:8080/oauth/error";
    private final String ADDITIONAL_SIGNUP_URL = "http://localhost:8080/signUp/additional-info"; // 추가 정보 입력 경로


    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        // 1. [핵심] 예외의 Cause가 우리가 만든 AdditionalInfoRequiredException인지 확인합니다.
        if (exception instanceof AdditionalInfoRequiredException) {
            // 2. 예외에서 데이터 Payload를 안전하게 추출합니다.
            OAuth2FailureData failureData = ((AdditionalInfoRequiredException) exception).getData();

            // 3. 추출한 실제 데이터를 사용하여 URL을 생성합니다. (TEMP 값 제거)
            String finalUrl = UriComponentsBuilder.fromUriString(ADDITIONAL_SIGNUP_URL)
                    .queryParam("providerId", failureData.getProviderId())
                    .queryParam("provider", failureData.getProvider())
                    .queryParam("nickname", failureData.getNickname())
                    .build()
                    .encode(StandardCharsets.UTF_8)
                    .toUriString();

            getRedirectStrategy().sendRedirect(request, response, finalUrl);
            return; // 로직 종료
        }

        // 3. 기타 모든 실패 (LOGIN_FAILED, invalid_client 등) 처리
        //    (EMAIL_CONSENT_REQUIRED가 아닐 경우)
        String finalUrl = UriComponentsBuilder.fromUriString(FRONTEND_ERROR_CALLBACK_URL)
                .queryParam("error", "LOGIN_FAILED")
                .build().encode(StandardCharsets.UTF_8).toUriString();

        getRedirectStrategy().sendRedirect(request, response, finalUrl);
    }
}