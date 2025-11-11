package com.example.carmmunity_capstone.controller;

import com.example.carmmunity_capstone.dto.WebSocketMessageDto;
import com.example.carmmunity_capstone.dto.WebSocketReadMessageDto;
import com.example.carmmunity_capstone.service.WebSocketChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class WebSocketChatController {

    private final WebSocketChatService webSocketChatService;

    /**
     * 1. 메시지 전송 (Send)
     * - 클라이언트가 `/app/chat.sendMessage`로 메시지를 'SEND'할 때 호출됨
     * - (보안 설정이 되어있다면, headerAccessor에서 유저 정보를 가져올 수 있음)
     */
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload WebSocketMessageDto wsMessage,
                            @AuthenticationPrincipal String currentUserUuid) {
        webSocketChatService.handleSendMessage(currentUserUuid, wsMessage);
    }

    /**
     * 2. 개별 메시지 읽음 처리 (Read)
     * - 클라이언트가 `/app/chat.readMessage`로 메시지를 'SEND'할 때 호출됨
     */
    @MessageMapping("/chat.readMessage")
    public void readMessage(@Payload WebSocketReadMessageDto wsReadMessage,
                            @AuthenticationPrincipal String currentUserUuid) {
        webSocketChatService.handleReadMessage(currentUserUuid, wsReadMessage);
    }
}