package com.example.carmmunity_capstone.dto;

import com.example.carmmunity_capstone.entity.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDto {
    private Long messageId;
    private Long roomId;
    private String senderUuid;
    private String senderNickname;
    private String senderProfilePath;
    private String content;
    private LocalDateTime sendTime;
    private long unreadCount; // 안 읽은 사람 수

    public static ChatMessageDto from(ChatMessage chatMessage, long unreadCount) {
        return ChatMessageDto.builder()
                .messageId(chatMessage.getChatMessageId())
                .roomId(chatMessage.getChatRoom().getChatRoomId())
                .senderUuid(chatMessage.getSender().getUserUuid())
                .senderNickname(chatMessage.getSender().getNickname())
                .senderProfilePath(chatMessage.getSender().getProfileImagePath())
                .content(chatMessage.getContent())
                .sendTime(chatMessage.getCreatedAt())
                .unreadCount(unreadCount)
                .build();
    }
}
