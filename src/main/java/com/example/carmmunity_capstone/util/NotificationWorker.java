package com.example.carmmunity_capstone.util;

import com.example.carmmunity_capstone.dto.NotificationDto;
import com.example.carmmunity_capstone.entity.Notification;
import com.example.carmmunity_capstone.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationWorker {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 개별 알림 전송 처리
     * Propagation.REQUIRES_NEW: 각 알림마다 독립적인 트랜잭션을 생성. 
     * 하나가 실패(Rollback)해도 다른 알림 트랜잭션에 영향을 주지 않음.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendIndividualNotification(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow();

        try {
            // 실제 전송 (WebSocket 예시)
            messagingTemplate.convertAndSend(
                    "/topic/notifications/" + notification.getRecipient().getUserUuid(),
                    NotificationDto.from(notification)
            );

            // 성공 시 상태 업데이트
            notification.markAsSent();

        } catch (Exception e) {
            log.error("알림 전송 실패 - ID: {}, 에러: {}", notificationId, e.getMessage());

            notification.incrementRetryCount();
            // 재시도 횟수가 초과되면 FAILED로 변경하여 스케줄러 대상에서 제외
            if (notification.getRetryCount() > 5) {
                notification.markAsFailed();
            }
            // 예외를 밖으로 던지지 않거나, 적절히 처리하여 스케줄러 루프가 깨지지 않게 함
        }
    }
}