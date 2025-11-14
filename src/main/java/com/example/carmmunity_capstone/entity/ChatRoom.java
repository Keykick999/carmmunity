package com.example.carmmunity_capstone.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "ChatRoom",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user1_uuid", "user2_uuid", "type"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_room_id")
    private Long chatRoomId;

    @Column(name = "room_name", length = 100)
    private String roomName; // 그룹 채팅방 이름

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private ChatRoomType type;

    // --- 1:1 채팅방 빠른 조회를 위한 필드 ---

    // 항상 ID가 더 작은(앞서는) 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user1_uuid")
    private User user1;

    // 항상 ID가 더 큰(뒤따르는) 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user2_uuid")
    private User user2;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // 양방향 연관관계
    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatParticipant> participants = new ArrayList<>();

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessage> messages = new ArrayList<>();
}