package com.example.carmmunity_capstone.util;

import com.example.carmmunity_capstone.entity.Notification;
import com.example.carmmunity_capstone.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final NotificationRepository notificationRepository;
    private final NotificationWorker notificationWorker;

    @Scheduled(fixedDelay = 1000)
    public void processPendingNotifications() {
        // pending 상태인 것 10개 가져오기
        List<Notification> pendingNotifications = notificationRepository.findPendingNotificationsForProcessing(PageRequest.of(0, 10));

        // 각각 알림 보내기
        for (Notification pendingNotification : pendingNotifications) {
            notificationWorker.sendIndividualNotification(pendingNotification.getNotificationId());
        }
    }
}