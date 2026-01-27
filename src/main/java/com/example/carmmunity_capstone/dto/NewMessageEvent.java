package com.example.carmmunity_capstone.dto;

import com.example.carmmunity_capstone.entity.NotificationType;
import com.example.carmmunity_capstone.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

/**
 * 메시지 저장 트랜잭션이 성공한 후 알림을 보내기 위한 이벤트
 */
@Getter
@RequiredArgsConstructor
public class NewMessageEvent {
    private final Long notificationId;
    private final NotificationType type;
    private final User sender;      // 메시지를 보낸 사람
    private final User recipient;   // 메시지를 받을 사람
    private final String message;
    private final String roomId;    // 메시지가 속한 채팅방 ID
    private final LocalDateTime createdAt;
}