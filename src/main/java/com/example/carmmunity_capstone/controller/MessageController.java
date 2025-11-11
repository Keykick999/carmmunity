package com.example.carmmunity_capstone.controller;

import com.example.carmmunity_capstone.dto.ChatMessageDto;
import com.example.carmmunity_capstone.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/messages")
public class MessageController {

    private final ChatService chatService;

    /**
     * [신규] 지난 메시지 조회 (API ②)
     * (GET /api/messages/101?page=0&size=30)
     */
    @GetMapping("/{roomId}")
    public ResponseEntity<Page<ChatMessageDto>> getMessages(
            @AuthenticationPrincipal String currentUserUuid,
            @PathVariable Long roomId,
            // (PageableDefault: 클라이언트가 page/size 안 보내면 기본값 설정)
            @PageableDefault(size = 30, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<ChatMessageDto> messages = chatService.getMessages(currentUserUuid, roomId, pageable);
        return ResponseEntity.ok(messages);
    }

    // (예외 처리 핸들러 추가 권장)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}