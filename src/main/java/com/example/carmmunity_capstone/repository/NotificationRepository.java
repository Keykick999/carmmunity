package com.example.carmmunity_capstone.repository;

import com.example.carmmunity_capstone.entity.Notification;
import com.example.carmmunity_capstone.entity.NotificationType;
import com.example.carmmunity_capstone.entity.User;
import org.springframework.data.domain.Pageable; // [추가] 페이징
import org.springframework.data.domain.Slice; // [추가] 페이징
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * 특정 수신자(recipient)의 *모든* 알림을 최신순으로 페이징하여 조회합니다.
     * (알림 목록 탭에서 사용)
     *
     * @param recipient 알림을 받은 사용자
     * @param pageable 페이징 정보 (예: PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")))
     * @return 페이징된 알림 목록 (Slice)
     */
    Slice<Notification> findByRecipient(User recipient, Pageable pageable);


    /**
     * 특정 수신자(recipient)의 *안 읽은(isRead = false)* 알림 개수만 카운트합니다.
     * (헤더 배지 카운트용)
     *
     * @param recipient 알림을 받은 사용자
     * @return 안 읽은 알림의 총 개수
     */
    int countByRecipientAndIsReadFalse(User recipient);


    /**
     * 특정 수신자(recipient)의 *모든 안 읽은(isRead = false)* 알림을
     * *일괄적으로 읽음(isRead = true)* 처리합니다.
     * (알림 목록 탭을 열었을 때 사용)
     *
     * @param recipient 알림을 받은 사용자
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.recipient = :recipient AND n.isRead = false")
    void markAllAsReadByRecipient(@Param("recipient") User recipient);

    Slice<Notification> findByRecipientAndTypeNot(User recipient, NotificationType type, Pageable pageable);

    int countByRecipientAndIsReadFalseAndTypeNot(User recipient, NotificationType type);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.recipient = :recipient AND n.isRead = false AND n.type != :type")
    void markAllAsReadByRecipientAndTypeNot(@Param("recipient") User recipient, @Param("type") NotificationType type);
}