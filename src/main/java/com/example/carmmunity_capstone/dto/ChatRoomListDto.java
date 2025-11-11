package com.example.carmmunity_capstone.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatRoomListDto {
    private Long roomId;
    private String roomName; // (1:1이면 상대방 이름, 그룹이면 그룹 이름)
    private String profileImagePath; // (1:1이면 상대방 프사, 그룹이면 그룹 프사)
    private String lastMessageContent;
    private LocalDateTime lastMessageTime;
    private boolean isUnread; // (핵심) 안 읽었으면 true (굵은 글씨 표시용)
}