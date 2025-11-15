package com.example.carmmunity_capstone.controller;

import com.example.carmmunity_capstone.dto.WebSocketMessageDto;
import com.example.carmmunity_capstone.dto.WebSocketReadMessageDto;
import com.example.carmmunity_capstone.service.WebSocketChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketChatController {

    private final WebSocketChatService webSocketChatService;

    /**
     * 클라이언트가 메시지를 전송할 때 호출됩니다.
     * 엔드포인트: /app/chat.sendMessage
     * 실제 요청 경로: /app/chat.sendMessage
     * @param wsMessage 클라이언트가 전송한 메시지 DTO
     */
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(
            java.security.Principal principal,
            @Payload WebSocketMessageDto wsMessage
    ) {
        String senderUuid = principal.getName();

        log.info("▶️ Message received from user: {}, Room: {}", senderUuid, wsMessage.getRoomId());

        // 서비스 로직 호출: DB 저장 및 브로드캐스트
        webSocketChatService.handleSendMessage(senderUuid, wsMessage);
    }

    /**
     * 클라이언트가 특정 메시지를 읽었음을 서버에 알릴 때 호출됩니다.
     * 엔드포인트: /app/chat.readMessage
     * 실제 요청 경로: /app/chat.readMessage
     * @param wsReadMessage 클라이언트가 전송한 읽음 처리 요청 DTO
     */
    @MessageMapping("/chat.readMessage")
    public void readMessage(
            java.security.Principal principal,
            @Payload WebSocketReadMessageDto wsReadMessage
    ) {
        String readerUuid = principal.getName();

        log.info("📚 Read receipt received from user: {}, Message ID: {}", readerUuid, wsReadMessage.getMessageId());

        // 서비스 로직 호출: 읽음 상태 DB 저장 및 읽음 이벤트 브로드캐스트
        webSocketChatService.handleReadMessage(readerUuid, wsReadMessage);
    }
}