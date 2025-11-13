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
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // StompHandler 주입
    private final StompHandler stompHandler;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 구독을 위해 사용되는 경로
        registry.enableSimpleBroker("/topic");
        // client에서 데이터 전송(발행)을 위해 사용되는 경로
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 연결을 위한 경로 설정
        registry.addEndpoint("/ws-stomp")
                .setAllowedOriginPatterns("*")  // 모든 도메인의 연결 허용
                .withSockJS();  // sockJS를 통해 웹 소켓을 지원하지 않는 브라우저도 웹소켓 통신이 가능하도록(cli, server 서로 통신을 위해 양쪽 모두 설정 필요)
    }

    // StompHandler를 인터셉터로 등록하는 메서드
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // STOMP 메시지(CONNECT, SEND, SUBSCRIBE 등)가 컨트롤러에 도달하기 전에
        // StompHandler가 먼저 가로채서 인증 처리를 수행합니다.
        // 특정 토픽에 대한 접근 제어, 메시지 내용/조작도 가능
        registration.interceptors(stompHandler);
    }
}