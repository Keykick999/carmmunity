package com.example.carmmunity_capstone.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {   // websocket에서 데이터를 주고 받을 때 인증된 사용자 정보 확인하기 위해 사용

    private final JwtTokenProvider jwtTokenProvider; // 제공해주신 JwtTokenProvider

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // 1. STOMP CONNECT:
        //    - 토큰 검증, Authentication 객체 생성
        //    - [중요] 생성된 Authentication을 *세션 속성*에 저장
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            log.info("STOMP CONNECT 요청 감지");
            String authorizationHeader = accessor.getFirstNativeHeader("Authorization");

            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String token = authorizationHeader.substring(7);
                if (jwtTokenProvider.validateToken(token)) {
                    String userUuid = jwtTokenProvider.getUserId(token);
                    System.out.println("userUuid in config file = " + userUuid);
                    Authentication authentication = new UsernamePasswordAuthenticationToken(userUuid, null, Collections.emptyList());

                    // 세션 속성(Session Attributes)에 인증 정보 저장
                    accessor.getSessionAttributes().put("userAuthentication", authentication);

                    // (setUser는 현재 CONNECT 메시지에만 적용되므로 세션 저장이 더 중요)
                    accessor.setUser(authentication);

                    log.info("STOMP 인증 성공. User: {}. 세션에 저장됨.", authentication.getName());
                } else {
                    log.warn("STOMP 인증 실패: 유효하지 않은 토큰");
                }
            } else {
                log.warn("STOMP 인증 실패: Authorization 헤더 없음");
            }
        }

        // STOMP SEND (메시지 전송) 또는 SUBSCRIBE (구독) 시:
        //    - 세션 속성에 저장된 Authentication 객체를 가져옴
        //    - [중요] 가져온 객체를 *현재 메시지의 헤더(accessor)*에 설정
        else if (StompCommand.SEND.equals(accessor.getCommand()) || StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {

            // 세션에서 저장해뒀던 인증 정보 검색
            Authentication authentication = (Authentication) accessor.getSessionAttributes().get("userAuthentication");
            System.out.println("authentication = " + authentication);

            if (authentication != null) {
                // 현재 메시지의 accessor에 User 설정
                // 이 작업이 있어야 @MessageMapping의 @AuthenticationPrincipal이 Principal을 찾을 수 있음
                accessor.setUser(authentication);
            } else {
                // (CONNECT가 안 되었거나 세션이 만료된 경우)
                log.warn("STOMP {}: 세션에서 인증 정보를 찾을 수 없습니다.", accessor.getCommand());
            }
        }

        return message;
    }
}