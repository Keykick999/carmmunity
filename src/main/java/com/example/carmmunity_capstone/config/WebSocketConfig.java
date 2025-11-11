package com.example.carmmunity_capstone.config; // 경로는 알맞게 수정하세요

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // WebSocket 메시지 브로커 활성화
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 1. 메시지 브로커 설정

        // "/topic"으로 시작하는 경로(destination)는 브로커가 직접 처리
        // (구독자들에게 메시지를 '방송'하는 경로)
        registry.enableSimpleBroker("/topic");

        // "/app"으로 시작하는 경로는 @MessageMapping이 붙은 컨트롤러가 처리
        // (클라이언트가 서버로 메시지를 '전송'할 때 사용하는 경로)
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 2. STOMP 엔드포인트 설정

        // 클라이언트가 WebSocket에 최초 접속할 때 사용할 엔드포인트
        // (예: ws://localhost:8080/ws-stomp)
        registry.addEndpoint("/ws-stomp")
                .setAllowedOriginPatterns("*") // (보안) 실제 프로덕션에서는 도메인을 명시해야 합니다.
                .withSockJS(); // WebSocket을 지원하지 않는 브라우저를 위한 SockJS 활성화
    }
}