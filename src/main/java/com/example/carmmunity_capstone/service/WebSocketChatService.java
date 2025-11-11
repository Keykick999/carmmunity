package com.example.carmmunity_capstone.service;

import com.example.carmmunity_capstone.dto.ChatMessageDto;
import com.example.carmmunity_capstone.dto.MessageReadEventDto;
import com.example.carmmunity_capstone.dto.WebSocketMessageDto;
import com.example.carmmunity_capstone.dto.WebSocketReadMessageDto;
import com.example.carmmunity_capstone.entity.*;
import com.example.carmmunity_capstone.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WebSocketChatService {

    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final MessageReadStatusRepository messageReadStatusRepository;

    // (핵심) WebSocket 브로드캐스팅을 위한 템플릿
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 1. 메시지 전송 (Send) 로직
     */
    @Transactional
    public void handleSendMessage(String senderUuid, WebSocketMessageDto wsMessage) {

        // 1. 엔티티 조회
        User sender = findUserByUuid(senderUuid);
        ChatRoom room = findRoomById(wsMessage.getRoomId());

        // [보안] 이 방의 참여자가 맞는지 확인
        chatParticipantRepository.findByUserAndChatRoom(sender, room)
                .orElseThrow(() -> new RuntimeException("채팅방에 참여한 사용자가 아닙니다."));

        // 2. ChatMessage 엔티티 생성 및 DB 저장
        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(room)
                .sender(sender)
                .content(wsMessage.getContent())
                .build();
        chatMessageRepository.save(chatMessage);

        // 3. '보낸 사람'은 즉시 읽음 처리 (MessageReadStatus 저장)
        MessageReadStatus readStatus = MessageReadStatus.builder()
                .chatMessage(chatMessage)
                .user(sender)
                .build();
        messageReadStatusRepository.save(readStatus);

        // 4. [안 읽은 수 계산]
        // (총 인원) - (읽은 인원: 방금 1명)
        long totalParticipants = chatParticipantRepository.countByChatRoom(room);
        long unreadCount = totalParticipants - 1; // 보낸 사람 1명만 읽음

        // 5. 브로드캐스팅할 DTO 생성
        ChatMessageDto broadcastDto = ChatMessageDto.from(chatMessage, unreadCount);

        // 6. /topic/room/{roomId}를 구독 중인 모든 클라이언트에게 메시지 '방송'
        messagingTemplate.convertAndSend("/topic/room/" + room.getChatRoomId(), broadcastDto);
    }

    /**
     * 2. 개별 메시지 읽음 처리 (Read) 로직
     */
    @Transactional
    public void handleReadMessage(String readerUuid, WebSocketReadMessageDto wsReadMessage) {

        // 1. 엔티티 조회
        User reader = findUserByUuid(readerUuid);
        ChatMessage message = chatMessageRepository.findById(wsReadMessage.getMessageId())
                .orElseThrow(() -> new RuntimeException("메시지를 찾을 수 없습니다."));
        ChatRoom room = message.getChatRoom();

        // 2. MessageReadStatus에 (messageId, readerId) 저장
        //    (DB UNIQUE 제약조건 덕분에 이미 읽었으면 중복 저장되지 않음)
        try {
            MessageReadStatus readStatus = MessageReadStatus.builder()
                    .chatMessage(message)
                    .user(reader)
                    .build();
            messageReadStatusRepository.save(readStatus);
        } catch (Exception e) {
            // (DataIntegrityViolationException)
            // 이미 읽음 처리된 메시지이므로, 그냥 무시하고 넘어감
            return;
        }

        // 3. [갱신된 안 읽은 수 계산]
        long totalParticipants = chatParticipantRepository.countByChatRoom(room);
        long readCount = messageReadStatusRepository.countByChatMessage(message);
        long unreadCount = totalParticipants - readCount;

        // 4. '읽음 이벤트' DTO 생성
        MessageReadEventDto readEventDto = new MessageReadEventDto(
                message.getChatMessageId(),
                reader.getUserUuid(),
                (unreadCount < 0 ? 0 : unreadCount)
        );

        // 5. /topic/room/{roomId}를 구독 중인 모든 클라이언트에게 '읽음 이벤트' 방송
        messagingTemplate.convertAndSend("/topic/room/" + room.getChatRoomId(), readEventDto);
    }

    // --- Helper Methods ---
    private User findUserByUuid(String userUuid) {
        return userRepository.findByUserUuid(userUuid)
                .orElseThrow(() -> new RuntimeException("User not found with uuid: " + userUuid));
    }

    private ChatRoom findRoomById(Long roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));
    }
}