package com.example.carmmunity_capstone.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "Friendship",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"requester_uuid", "receiver_uuid"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "friendship_id")
    private Long friendshipId;

    // 요청을 보낸 사용자 (ManyToOne 관계)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_uuid", nullable = false)
    private User requester;

    // 요청을 받은 사용자 (ManyToOne 관계)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_uuid", nullable = false)
    private User receiver;

    // 관계 상태 (PENDING, ACCEPTED, DECLINED, BLOCKED)
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private FriendshipStatus status;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}