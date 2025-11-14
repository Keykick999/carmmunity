// com/example/carmmunity_capstone/dto/OAuthAttributes.java

import com.example.carmmunity_capstone.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;
import java.util.UUID;

@Getter
@Builder
public class OAuthAttributes {
    private Map<String, Object> attributes;
    private String nameAttributeKey;
    private String name;
    private String email;
    private String picture;
    private String provider;
    private String providerId;

    public static OAuthAttributes of(String registrationId, String userNameAttributeName, Map<String, Object> attributes) {
        if ("naver".equals(registrationId)) {
            return ofNaver(userNameAttributeName, attributes);
        }
        if ("kakao".equals(registrationId)) {
            return ofKakao(userNameAttributeName, attributes);
        }
        return ofGoogle(userNameAttributeName, attributes);
    }

    private static OAuthAttributes ofGoogle(String userNameAttributeName, Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .name((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .picture((String) attributes.get("picture"))
                .provider("google")
                .providerId((String) attributes.get(userNameAttributeName))
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    @SuppressWarnings("unchecked")
    private static OAuthAttributes ofNaver(String userNameAttributeName, Map<String, Object> attributes) {
        Map<String, Object> response = (Map<String, Object>) attributes.get(userNameAttributeName);

        return OAuthAttributes.builder()
                .name((String) response.get("name"))
                .email((String) response.get("email"))
                .picture((String) response.get("profile_image"))
                .provider("naver")
                .providerId((String) response.get("id"))
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    @SuppressWarnings("unchecked")
    private static OAuthAttributes ofKakao(String userNameAttributeName, Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        return OAuthAttributes.builder()
                .name((String) profile.get("nickname"))
                .email((String) kakaoAccount.get("email"))
                .picture((String) profile.get("profile_image_url"))
                .provider("kakao")
                .providerId(String.valueOf(attributes.get(userNameAttributeName))) // "id"
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    /**
     * 신규 User 엔티티 생성
     */
    public User toEntity() {
        return User.builder()
                .name(name)
                .email(email)
                .nickname(name) // (정책) 닉네임은 우선 이름(name)으로
                .profileImagePath(picture)
                .provider(provider)
                .providerId(providerId)
                // password는 nullable=true로 변경했다고 가정
                // 만약 NOT NULL 제약조건이 있다면 임의의 값을 넣어줘야 함
                .password(UUID.randomUUID().toString()) // (보안) 비번 필드 임의의 값으로 채우기
                .isPrivate(false)
                // userUuid는 @PrePersist로 자동 생성
                // userId (자체 로그인 ID)는 null
                .build();
    }

    /**
     * 기존 User 엔티티 업데이트 (이름, 프로필)
     */
    public User update(User user) {
        user.setName(this.name);
        user.setProfileImagePath(this.picture);
        return user;
    }
}