package com.example.carmmunity_capstone.repository;

import com.example.carmmunity_capstone.entity.ChatParticipant;
import com.example.carmmunity_capstone.entity.ChatRoom;
import com.example.carmmunity_capstone.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {

    // 채팅방 생성 시 사용
    // List<ChatParticipant> findByUser(User user);

    /**
     * [N+1 문제 최적화 - 채팅 목록 조회용]
     * ChatParticipant를 찾을 때, 연관된 ChatRoom 객체도 즉시 로딩합니다.
     */
    @Query("SELECT cp FROM ChatParticipant cp JOIN FETCH cp.chatRoom WHERE cp.user = :user")
    List<ChatParticipant> findByUserWithChatRoom(@Param("user") User user);

    /**
     * [추가] 특정 채팅방의 총 인원 수를 카운트합니다.
     */
    long countByChatRoom(ChatRoom chatRoom);

    /**
     * [추가] 사용자와 채팅방으로 특정 참여자 정보를 찾습니다. (권한 확인 및 '책갈피' 업데이트용)
     */
    Optional<ChatParticipant> findByUserAndChatRoom(User user, ChatRoom chatRoom);
}