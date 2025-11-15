package com.example.carmmunity_capstone.service;

import com.example.carmmunity_capstone.dto.FollowEvent;
import com.example.carmmunity_capstone.dto.NewMessageEvent;
import com.example.carmmunity_capstone.dto.NotificationDto;
import com.example.carmmunity_capstone.entity.Notification;
import com.example.carmmunity_capstone.entity.NotificationType;
import com.example.carmmunity_capstone.entity.User;
import com.example.carmmunity_capstone.repository.NotificationRepository;
import com.example.carmmunity_capstone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.annotation.Propagation;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    /**
     * [핵심 수정]
     * public sendNotification(...) 메서드 대신,
     * FollowEvent를 리스닝(구독)하는 이벤트 핸들러 메서드를 사용합니다.
     *
     * @TransactionalEventListener :
     * 이 어노테이션이 붙어있기 때문에, 이벤트를 발행한(@Transactional) 메서드의 트랜잭션이
     * [성공적으로 커밋(Commit)된 후에만] 이 메서드가 호출됩니다.
     * 이것이 '유령 알림'을 완벽하게 방지하는 핵심입니다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFollowEvent(FollowEvent event) {
        log.info("트랜잭션 커밋 완료, 팔로우 이벤트 수신. 알림 전송 시작.");

        User sender = event.getSender();
        User receiver = event.getReceiver();

        // 수신자 유효성 검사 (알림 유발자와 수신자가 같은 경우 등)
        if (receiver == null || sender.getUserUuid().equals(receiver.getUserUuid())) {
            log.warn("알림 수신자가 유효하지 않습니다. (Sender: {}, Receiver: {})", sender.getUserUuid(), receiver != null ? receiver.getUserUuid() : "null");
            return;
        }

        log.info("팔로우 이벤트 수신 (DB 저장 및 전송): {} -> {}", sender.getNickname(), receiver.getNickname());


        // 2. 알림 엔티티 생성 (DB 저장용)
        Notification newNotification = Notification.builder()
                .recipient(receiver)
                .sender(sender)
                .type(event.getType())
                .message(event.getMessage())
                .build();

        // 3. 알림을 DB에 저장
        Notification savedNotification = notificationRepository.save(newNotification);

        // 4. 실시간 전송 DTO 생성 (DB 엔티티 기반)
        // (NotificationDto.from 정적 메서드 사용)
        NotificationDto notificationDto = NotificationDto.from(savedNotification);

        // 5. 클라이언트로 실제 알림 전송
        sendNotification(receiver, notificationDto);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMessageEvent(NewMessageEvent event) {
        // 유효성 검사
        User sender = event.getSender();
        User recipient = event.getRecipient();

        if (recipient == null || sender.getUserUuid().equals(recipient.getUserUuid())) {
            log.warn("메시지 알림 수신자가 유효하지 않습니다. (Sender: {}, Receiver: {})", sender.getUserUuid(), recipient != null ? recipient.getUserUuid() : "null");
            return;
        }

        log.info("메시지 이벤트 수신 (DB 저장 및 전송): {} -> {}", event.getSender().getNickname(), event.getRecipient().getNickname());

        // 알림 엔티티 생성 (DB에 저장하기 위해)
        Notification newNotification = Notification.builder()
                .recipient(event.getRecipient())
                .sender(event.getSender())
                .type(NotificationType.NEW_MESSAGE)
                .message("새로운 메시지가 도착했습니다.")
                .build();

        // 알림을 DB에 저장
        Notification savedNotification = notificationRepository.save(newNotification);

        // 실시간 전송을 위한 DTO 생성
        NotificationDto notificationDto = NotificationDto.builder()
                .notificationId(savedNotification.getNotificationId())
                .type(savedNotification.getType())
                .senderUserUuid(savedNotification.getSender().getUserUuid())
                .senderNickname(savedNotification.getSender().getNickname())
                .senderProfileImage(savedNotification.getSender().getProfileImagePath())
                .message(savedNotification.getMessage())
                .isRead(savedNotification.isRead())
                .createdAt(savedNotification.getCreatedAt())
                .roomId(event.getRoomId())
                .build();

        // 4. 클라이언트로 실제 알림 전송
        sendNotification(event.getRecipient(), notificationDto);
    }

    private void sendNotification(User receiver, NotificationDto dto) {
        // 1. 수신자 null 체크 (필수)
        if (receiver == null || receiver.getUserUuid() == null) {
            log.error("sendNotification 실패: 수신자 또는 수신자 UUID가 null입니다. DTO: {}", dto.getMessage());
            return;
        }

        String destination = "/topic/notifications/" + receiver.getUserUuid();

        try {
            messagingTemplate.convertAndSend(destination, dto);
        } catch (Exception e) {
            log.error("WebSocket 알림 전송 실패: (User: {}), Error: {}", receiver.getUserUuid(), e.getMessage(), e);
        }
    }

    /**
     * [API] 안 읽은 알림 개수 조회 (FOLLOW_REQUEST 제외)
     * (클라이언트가 로그인 시 헤더 배지 카운트를 가져갈 때 사용)
     */
    @Transactional(readOnly = true)
    public int getUnreadNotificationCount(String userUuid) {
        User user = findUserByUuid(userUuid);

        // 1. 제외할 알림 타입 목록을 생성
        List<NotificationType> excludedTypes = List.of(
                NotificationType.FOLLOW_REQUEST, // "팔로우 요청" 제외
                NotificationType.NEW_MESSAGE     // "새 메시지" 제외
        );

        // 2. NotIn 메서드 호출
        return notificationRepository.countByRecipientAndIsReadFalseAndTypeNotIn(
                user,
                excludedTypes
        );
    }

    /**
     * [API] 알림 목록 페이징 조회 (FOLLOW_REQUEST 제외)
     * (클라이언트가 '알림' 탭을 클릭했을 때 사용)
     */
    @Transactional(readOnly = true)
    public Slice<NotificationDto> getNotifications(String userUuid, Pageable pageable) {
        User user = findUserByUuid(userUuid);

        // 제외할 알림 타입들
        List<NotificationType> excludedTypes = List.of(
                NotificationType.FOLLOW_REQUEST,
                NotificationType.NEW_MESSAGE
        );

        // "팔로우 요청"은 별도 탭으로 관리하므로, '알림 목록'에서는 제외
        Slice<Notification> notifications = notificationRepository.findByRecipientAndTypeNotIn(
                user,
                excludedTypes,
                pageable
        );

        System.out.println("notifications = " + notifications);

        // DB 엔티티(Notification)를 클라이언트 DTO(NotificationDto)로 변환하여 반환
        return notifications.map(NotificationDto::from);
    }

    /**
     * [API] 모든 알림을 '읽음'으로 처리 (FOLLOW_REQUEST 제외)
     * (클라이언트가 '알림' 탭을 열었을 때 호출)
     */
    @Transactional
    public void markAllAsRead(String userUuid) {
        User user = findUserByUuid(userUuid);

        // "팔로우 요청" 알림은 건드리지 않고 나머지 모든 알림만 읽음 처리
        notificationRepository.markAllAsReadByRecipientAndTypeNot(
                user,
                NotificationType.FOLLOW_REQUEST
        );
    }

    private User findUserByUuid(String userUuid) {
        return userRepository.findByUserUuid(userUuid)
                .orElseThrow(() -> new RuntimeException("User not found with uuid: " + userUuid));
    }
}