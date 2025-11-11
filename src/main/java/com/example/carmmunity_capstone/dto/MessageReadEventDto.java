package com.example.carmmunity_capstone.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MessageReadEventDto {
    private Long messageId;     // 읽힌 메시지 ID
    private String readerUuid;  // 메시지를 읽은 사람의 UUID
    private long unreadCount;   // 갱신된 '안 읽은 수' (예: 3 -> 2)
}