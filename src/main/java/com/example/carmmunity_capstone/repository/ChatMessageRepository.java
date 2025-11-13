package com.example.carmmunity_capstone.repository;

import com.example.carmmunity_capstone.entity.ChatMessage;
import com.example.carmmunity_capstone.entity.ChatRoom;
import com.example.carmmunity_capstone.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    /**
     * 특정 채팅방(roomId)에서, 특정 사용자(userId)가 읽지 않은 메시지 수를 계산합니다.
     * (조건: 보낸 사람이 내가 아니고, 내 MessageReadStatus가 없는 메시지)
     * [중요]: 'cm.sender.userId', 'mrs.user.userId'는 User 엔티티의 @Id 필드명이어야 합니다.
     */
    @Query("SELECT COUNT(cm) FROM ChatMessage cm " +
            "WHERE cm.chatRoom.chatRoomId = :roomId " +
            "AND cm.sender.userId != :userId " + // 내가 보낸 메시지는 제외
            "AND NOT EXISTS (" +
            "    SELECT 1 FROM MessageReadStatus mrs " +
            "    WHERE mrs.chatMessage = cm AND mrs.user.userId = :userId" +
            ")")
    int countUnreadMessagesForUserInRoom(
            @Param("roomId") Long roomId,
            @Param("userId") String userId // User 엔티티의 @Id가 String이므로 String
    );
}