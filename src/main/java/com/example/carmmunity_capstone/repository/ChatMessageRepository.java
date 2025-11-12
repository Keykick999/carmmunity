package com.example.carmmunity_capstone.repository;

import com.example.carmmunity_capstone.entity.ChatMessage;
import com.example.carmmunity_capstone.entity.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.userdetails.User;

import java.util.List;
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

    @Query("SELECT m FROM ChatMessage m " +
            "WHERE m.chatRoom = :room AND NOT EXISTS (" +
            "  SELECT 1 FROM MessageReadStatus s " +
            "  WHERE s.chatMessage = m AND s.user = :user" +
            ")")
    List<ChatMessage> findUnreadMessagesByUserInRoom(@Param("room") ChatRoom room, @Param("user") User user);
}