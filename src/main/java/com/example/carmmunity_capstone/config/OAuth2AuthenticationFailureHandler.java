// com/example/carmmunity_capstone/config/OAuth2AuthenticationFailureHandler.java
// (config 패키지에 생성)

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

    // (설정) 프론트엔드 에러 콜백 URL
    // TODO: 이 주소를 실제 프론트엔드 주소로 변경해야 합니다.
    private final String FRONTEND_ERROR_CALLBACK_URL = "http://localhost:3000/oauth/error";

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {

        String errorCode = "LOGIN_FAILED";

        // CustomOAuth2UserService에서 발생시킨 예외 메시지 확인
        if ("EMAIL_CONSENT_REQUIRED".equals(exception.getMessage())) {
            errorCode = "EMAIL_CONSENT_REQUIRED";
        }

        // 4. 프론트엔드 에러 페이지로 리다이렉트
        String targetUrl = UriComponentsBuilder.fromUriString(FRONTEND_ERROR_CALLBACK_URL)
                .queryParam("error", errorCode)
                .build()
                .encode(StandardCharsets.UTF_8)
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}