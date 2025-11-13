package com.example.carmmunity_capstone.controller;

import com.example.carmmunity_capstone.dto.NotificationDto;
import com.example.carmmunity_capstone.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 1. [API] 안 읽은 '기타 알림' 개수 조회 (헤더 배지용)
     * (팔로우 요청은 제외, 메시지는 별도 카운트)
     * @param currentUserUuid (토큰)
     * @return 안 읽은 알림 개수 (int)
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Integer> getUnreadNotificationCount(
            @AuthenticationPrincipal String currentUserUuid
    ) {
        int count = notificationService.getUnreadNotificationCount(currentUserUuid);
        return ResponseEntity.ok(count);
    }

    /**
     * 2. [API] '알림' 탭 목록 조회 (팔로우 요청 제외, 최신순)
     * @param currentUserUuid (토큰)
     * @param pageable (자동 주입: ?page=0&size=10)
     * @return 알림 DTO 목록 (Slice)
     */
    @GetMapping
    public ResponseEntity<Slice<NotificationDto>> getNotificationList(
            @AuthenticationPrincipal String currentUserUuid,
            // (최신순으로 정렬)
            @PageableDefault(size = 15, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Slice<NotificationDto> notifications = notificationService.getNotifications(currentUserUuid, pageable);
        return ResponseEntity.ok(notifications);
    }

    /**
     * 3. [API] '알림' 탭의 모든 알림 '읽음' 처리
     * (사용자가 알림 탭을 열었을 때 호출)
     * @param currentUserUuid (토큰)
     * @return 성공 (200 OK)
     */
    @PostMapping("/read-all")
    public ResponseEntity<Void> readAllNotifications(
            @AuthenticationPrincipal String currentUserUuid
    ) {
        notificationService.markAllAsRead(currentUserUuid);
        return ResponseEntity.ok().build();
    }
}