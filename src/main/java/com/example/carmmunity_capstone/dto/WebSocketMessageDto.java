package com.example.carmmunity_capstone.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class WebSocketMessageDto {
    private Long roomId;    // 메시지를 보낼 채팅방 ID
    private String content; // 메시지 내용
}