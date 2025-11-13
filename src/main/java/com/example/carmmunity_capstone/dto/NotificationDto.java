package com.example.carmmunity_capstone.dto;

import com.example.carmmunity_capstone.entity.Notification;
import com.example.carmmunity_capstone.entity.NotificationType; // [수정] entity 패키지의 Enum 임포트
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NotificationDto {

    // [신규] DB 엔티티의 ID
    private Long notificationId;

    // (기존) 알림 유형
    private NotificationType type;

    // (기존) 알림 발생시킨 사용자 정보
    private String senderUserUuid;
    private String senderNickname;
    private String senderProfileImage;

    // (기존) 메시지 본문
    private String message;

    // (기존) 메시지 알림용 채팅방 ID
    private String roomId;

    // [신규] 읽음 여부
    private boolean isRead;

    // [신규] 알림 발생 시간
    private LocalDateTime createdAt;

    // --- (기존 Enum 정의는 삭제됨) ---

    /**
     * [신규] Notification 엔티티를 NotificationDto로 변환하는 정적 메서드
     * (NotificationService에서 사용)
     */
    public static NotificationDto from(Notification notification) {
        // sender가 null일 수 있음 (시스템 알림 등)
        String senderUuid = (notification.getSender() != null) ? notification.getSender().getUserUuid() : null;
        String senderNickname = (notification.getSender() != null) ? notification.getSender().getNickname() : null;
        String senderProfileImage = (notification.getSender() != null) ? notification.getSender().getProfileImagePath() : null;

        return NotificationDto.builder()
                .notificationId(notification.getNotificationId())
                .type(notification.getType())
                .senderUserUuid(senderUuid)
                .senderNickname(senderNickname)
                .senderProfileImage(senderProfileImage)
                .message(notification.getMessage())
                // .roomId(notification.getRelatedRoomId()) // (만약 엔티티에 roomId를 저장한다면)
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}