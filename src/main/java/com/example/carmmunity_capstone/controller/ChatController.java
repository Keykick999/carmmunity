package com.example.carmmunity_capstone.controller; // 경로는 알맞게 수정하세요

import com.example.carmmunity_capstone.dto.ChatRoomListDto;
import com.example.carmmunity_capstone.dto.CreateChatRoomRequest;
import com.example.carmmunity_capstone.dto.CreateChatRoomResponse;
import com.example.carmmunity_capstone.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chats")
public class ChatController {

    private final ChatService chatService;

    /**
     * 1:1 또는 그룹 채팅방 생성 API
     * (1:1인 경우, 기존 방이 있으면 조회, 없으면 생성)
     */
    @PostMapping("/create")
    public ResponseEntity<CreateChatRoomResponse> createChatRoom(
            @AuthenticationPrincipal String currentUserUuid, // 현재 로그인한 유저 UUID
            @RequestBody CreateChatRoomRequest request) {
        System.out.println("request = " + request);
        System.out.println("currentUserUuid = " + currentUserUuid);

        CreateChatRoomResponse response = chatService.createChatRoom(currentUserUuid, request);
        System.out.println("response = " + response);
        return ResponseEntity.ok(response);
    }

    /**
     * 2. 채팅 목록 조회 (API ①)
     */
    @GetMapping("/my-rooms")
    public ResponseEntity<List<ChatRoomListDto>> getMyChatRooms(
            @AuthenticationPrincipal String currentUserUuid) {
        return ResponseEntity.ok(chatService.getChatRoomList(currentUserUuid));
    }

    /**
     * 3. 채팅방 읽음 처리 (책갈피) (API ③)
     * (채팅방 입장 시 호출됨)
     */
    @PostMapping("/{roomId}/read")
    public ResponseEntity<Void> readChatRoom(
            @AuthenticationPrincipal String currentUserUuid,
            @PathVariable Long roomId) {
        chatService.readChatRoom(currentUserUuid, roomId);
        return ResponseEntity.ok().build();
    }

    // (예외 처리 핸들러 추가 권장)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}