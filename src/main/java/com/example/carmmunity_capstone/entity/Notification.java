package com.example.carmmunity_capstone.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_uuid", nullable = false)
    private User recipient; // (필수) 알림을 받는 사람

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_uuid")
    private User sender; // (선택) 알림을 유발한 사람 (시스템 알림은 null)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type; // (DTO의 Enum 재사용)

    @Column(nullable = false)
    private String message; // (알림 메시지)

    @Column(nullable = false)
    @Builder.Default
    private boolean isRead = false; // [핵심] 읽음 여부 (기본값: false)

    @CreatedDate
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private NotificationStatus status = NotificationStatus.PENDING;

    @Builder.Default
    private int retryCount = 0;

    private LocalDateTime sentAt; // 실제 발송 성공 시각

    public void markAsSent() {
        this.status = NotificationStatus.SENT;
    }

    public void incrementRetryCount() {
        this.retryCount++;
    }

    public void markAsFailed() {
        this.status = NotificationStatus.FAILED;
    }
}