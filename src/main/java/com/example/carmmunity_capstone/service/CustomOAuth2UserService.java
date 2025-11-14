package com.example.carmmunity_capstone.service;// com/example/carmmunity_capstone/service/CustomOAuth2UserService.java
// (service 패키지. userDetailsService와 같은 위치)

import com.example.carmmunity_capstone.entity.User;
import com.example.carmmunity_capstone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        // 1. JSON 파싱
        OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());

        // 2. 이메일 미동의 처리
        if (attributes.getEmail() == null) {
            // FailureHandler가 이 예외를 캐치할 것입니다.
            throw new OAuth2AuthenticationException("EMAIL_CONSENT_REQUIRED");
        }

        // 3. DB에 저장 또는 업데이트
        User user = saveOrUpdate(attributes);

        // 4. 인증 객체 반환 (SuccessHandler가 이 객체를 받음)
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                attributes.getAttributes(),
                attributes.getNameAttributeKey()
        );
    }

    private User saveOrUpdate(OAuthAttributes attributes) {
        Optional<User> userOptional = userRepository.findByEmail(attributes.getEmail());

        User user;
        if (userOptional.isPresent()) {
            user = userOptional.get();
            // (정책) 기존 유저의 이름, 프로필 사진을 소셜 정보로 덮어쓰기
            user = attributes.update(user);
        } else {
            // 신규 유저
            user = attributes.toEntity();
        }

        return userRepository.save(user);
    }
}