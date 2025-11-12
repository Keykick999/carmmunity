package com.example.carmmunity_capstone.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class WebSocketReadMessageDto {
    private Long messageId; // 내가 '읽은' 메시지의 ID
}