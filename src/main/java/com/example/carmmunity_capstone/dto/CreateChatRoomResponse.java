package com.example.carmmunity_capstone.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateChatRoomResponse {
    private Long roomId;
    private String roomName; // (1:1이면 상대방 이름, 그룹이면 그룹 이름)
}