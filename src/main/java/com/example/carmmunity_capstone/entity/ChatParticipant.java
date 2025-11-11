package com.example.carmmunity_capstone.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "ChatParticipant",
        uniqueConstraints = {
                // 한 유저는 한 채팅방에 한 번만 참여 가능
                @UniqueConstraint(columnNames = {"user_id", "chat_room_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_participant_id")
    private Long chatParticipantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @CreatedDate
    @Column(name = "joined_at", updatable = false)
    private LocalDateTime joinedAt;

    /**
     * [채팅 목록 최적화 필드]
     * 이 유저가 이 채팅방에서 마지막으로 "확인"한 메시지의 ID.
     * 채팅방 목록의 "안 읽은 수" 계산에 사용됩니다.
     * 유저가 방에 입장할 때 이 값을 방의 최신 메시지 ID로 업데이트합니다.
     */
    @Column(name = "last_read_message_id")
    private Long lastReadMessageId;
}