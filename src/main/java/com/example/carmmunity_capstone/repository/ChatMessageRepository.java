package com.example.carmmunity_capstone.repository;

import com.example.carmmunity_capstone.entity.ChatMessage;
import com.example.carmmunity_capstone.entity.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * [추가] 채팅방의 '마지막' 메시지 1개를 가져옵니다. (채팅 목록 표기용)
     */
    Optional<ChatMessage> findTopByChatRoomOrderByCreatedAtDesc(ChatRoom chatRoom);

    /**
     * [추가] 채팅방의 메시지 목록을 '페이지네이션'으로 조회합니다. (과거 내역 로딩용)
     */
    Page<ChatMessage> findByChatRoomOrderByCreatedAtDesc(ChatRoom chatRoom, Pageable pageable);
}