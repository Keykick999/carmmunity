package com.example.carmmunity_capstone.dto;

import com.example.carmmunity_capstone.entity.ChatMessage;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatMessageDto {
    private Long messageId;
    private String senderUuid;
    private String senderNickname;
    private String senderProfileImage;
    private String content;
    private LocalDateTime sentAt;
    private long unreadCount; // (핵심) 안 읽은 사람 수 (카톡 숫자)

    /**
     * Entity를 DTO로 변환하는 정적 팩토리 메서드
     */
    public static ChatMessageDto from(ChatMessage message, long unreadCount) {
        return ChatMessageDto.builder()
                .messageId(message.getChatMessageId())
                .senderUuid(message.getSender().getUserUuid())
                .senderNickname(message.getSender().getNickname())
                .senderProfileImage(message.getSender().getProfileImagePath())
                .content(message.getContent())
                .sentAt(message.getCreatedAt())
                .unreadCount(unreadCount)
                .build();
    }
}