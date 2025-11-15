package com.example.carmmunity_capstone.service;

import com.example.carmmunity_capstone.dto.*;
import com.example.carmmunity_capstone.entity.*;
import com.example.carmmunity_capstone.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketChatService {

    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final MessageReadStatusRepository messageReadStatusRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ApplicationEventPublisher eventPublisher; // [신규]


    /**
     * 1. 메시지 전송 (Send) 로직
     */
    @Transactional
    public void handleSendMessage(String senderUuid, WebSocketMessageDto wsMessage) {
        // 1. 엔티티 조회
        User sender = findUserByUuid(senderUuid);
        ChatRoom room = findRoomById(wsMessage.getRoomId());

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

        // 4. 알림 이벤트를 발행하기 위해 *모든* 참여자 조회
        List<ChatParticipant> participants = chatParticipantRepository.findByChatRoom(room);

        // 5. 받는 사람들에게 알림 이벤트 발행
        for (ChatParticipant participant : participants) {
            User recipient = participant.getUser();

            // 보낸 사람(sender) 자신에게는 알림을 보내지 않습니다.
            if (!recipient.getUserUuid().equals(senderUuid)) {
                log.info("Publishing NewMessageEvent for recipient: {}", recipient.getNickname());

                // 이 이벤트는 @TransactionalEventListener가 수신하여
                // 트랜잭션이 *커밋된 후* NotificationService에서 알림을 보냅니다.
                eventPublisher.publishEvent(new NewMessageEvent(
                        sender,
                        recipient,
                        room.getChatRoomId().toString() // (roomId는 Long/String 타입에 맞게 전달)
                ));
            }
        }

        // 6. [안 읽은 수 계산]
        long totalParticipants = participants.size(); // [수정됨]
        long unreadCount = totalParticipants - 1; // 보낸 사람 1명만 읽음

        // 5. 브로드캐스팅할 DTO 생성
        ChatMessageDto broadcastDto = ChatMessageDto.from(chatMessage, unreadCount);


        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                // 이 코드는 트랜잭션이 DB에 완전히 반영된(커밋된) 후에 실행됩니다.
                messagingTemplate.convertAndSend("/topic/room/" + room.getChatRoomId(), broadcastDto);
            }
        });
    }


    /**
     * 2. 개별 메시지 읽음 처리 (Read) 로직
     */
    @Transactional
    public void handleReadMessage(String readerUuid, WebSocketReadMessageDto wsReadMessage) {

        // 1. 엔티티 조회
        User reader = findUserByUuid(readerUuid);

        // 🚩 [수정] 경합 조건이 해결되었으므로, .isEmpty() 체크를 제거하고 .orElseThrow()로 롤백합니다.
        // (이제 메시지를 못 찾으면 그건 진짜 예외 상황임)
        ChatMessage message = chatMessageRepository.findById(wsReadMessage.getMessageId())
                .orElseThrow(() -> new RuntimeException("메시지를 찾을 수 없습니다."));

        ChatRoom room = message.getChatRoom();

        // 2. MessageReadStatus에 (messageId, readerId) 저장
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