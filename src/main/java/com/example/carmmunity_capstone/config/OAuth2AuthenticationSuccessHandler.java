// com/example/carmmunity_capstone/config/OAuth2AuthenticationSuccessHandler.java
// (config 패키지에 생성)

import com.example.carmmunity_capstone.config.JwtTokenProvider;
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

    // (설정) 프론트엔드 콜백 URL
    // TODO: 이 주소를 실제 프론트엔드 주소로 변경해야 합니다.
    private final String FRONTEND_CALLBACK_URL = "http://localhost:3000/oauth/callback";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // 1. 인증 객체에서 OAuth2User와 registrationId 추출
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;
        String registrationId = authToken.getAuthorizedClientRegistrationId();

        // 2. OAuthAttributes DTO로 파싱 (email을 얻기 위함)
        // (CustomOAuth2UserService에서 이미 파싱했지만, SuccessHandler는 DB 상태만 알 뿐이므로 재파싱)
        OAuthAttributes attributes = OAuthAttributes.of(
                registrationId,
                oAuth2User.getNameAttributeKey(),
                oAuth2User.getAttributes()
        );

        // 3. 이메일로 DB에서 User 조회 (반드시 존재해야 함)
        User user = userRepository.findByEmail(attributes.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + attributes.getEmail()));

        // 4. JWT 토큰 생성 (JwtTokenProvider 사용)
        // JwtAuthenticationFilter를 보니 userUuid를 사용하므로, user.getUserUuid()를 넘김
        String accessToken = jwtTokenProvider.createToken(user.getUserUuid());

        // (선택) Refresh Token도 생성한다면 여기서 생성
        // String refreshToken = ...

        // 5. 프론트엔드로 리다이렉트 (URL 쿼리 파라미터에 토큰 추가)
        String targetUrl = UriComponentsBuilder.fromUriString(FRONTEND_CALLBACK_URL)
                .queryParam("accessToken", accessToken)
                // .queryParam("refreshToken", refreshToken) // 리프레시 토큰도 보낸다면
                .build()
                .encode(StandardCharsets.UTF_8)
                .toUriString();

        // 6. 리다이렉트
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}