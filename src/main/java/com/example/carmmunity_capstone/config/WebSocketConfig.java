package com.example.carmmunity_capstone.config; // 경로는 알맞게 수정하세요

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor // 🚩 [추가]
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // 🚩 [추가] StompHandler 주입
    private final StompHandler stompHandler;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 1. 메시지 브로커 설정 (기존과 동일)
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 2. STOMP 엔드포인트 설정 (기존과 동일)
        registry.addEndpoint("/ws-stomp")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    // 🚩 [추가] StompHandler를 인터셉터로 등록하는 메서드
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // STOMP 메시지(CONNECT, SEND, SUBSCRIBE 등)가 컨트롤러에 도달하기 전에
        // StompHandler가 먼저 가로채서 인증 처리를 수행합니다.
        registration.interceptors(stompHandler);
    }
}