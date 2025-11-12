package com.example.carmmunity_capstone.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Getter
@NoArgsConstructor
@ToString
public class CreateChatRoomRequest {

    /**
     * 대화 상대의 UUID 리스트
     * - 1:1 채팅 시: 1개
     * - 그룹 채팅 시: 2개 이상 (보통 클라이언트가 자신은 제외하고 보냄)
     */
    private List<String> targetUserUuids;

    /**
     * 그룹 채팅방 이름
     * (1:1 채팅 시에는 null이거나 무시됨)
     */
    private String roomName;
}