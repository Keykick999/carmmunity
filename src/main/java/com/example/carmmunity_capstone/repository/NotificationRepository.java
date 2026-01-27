package com.example.carmmunity_capstone.repository;

import com.example.carmmunity_capstone.entity.Notification;
import com.example.carmmunity_capstone.entity.NotificationStatus;
import com.example.carmmunity_capstone.entity.NotificationType;
import com.example.carmmunity_capstone.entity.User;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Pageable; // [추가] 페이징
import org.springframework.data.domain.Slice; // [추가] 페이징
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

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

    Slice<Notification> findByRecipientAndTypeNotIn(User recipient, Collection<NotificationType> types, Pageable pageable);

    int countByRecipientAndIsReadFalseAndTypeNotIn(User recipient, Collection<NotificationType> types);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.recipient = :recipient AND n.isRead = false AND n.type != :type")
    void markAllAsReadByRecipientAndTypeNot(@Param("recipient") User recipient, @Param("type") NotificationType type);

    /**
     * PENDING 상태인 알림을 비관적 락을 걸어서 가져옵니다.
     * SKIP LOCKED 설정을 통해 이미 다른 쓰레드가 락을 건 행은 무시하고 지나갑니다.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE) // DB 수준에서 SELECT ... FOR UPDATE 적용
    @QueryHints({
            @QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2") // -2는 'SKIP LOCKED'를 의미 (PostgreSQL/Oracle/MySQL 8.0+)
    })
    @Query("SELECT n FROM Notification n WHERE n.status = 'PENDING' ORDER BY n.createdAt ASC")
    List<Notification> findPendingNotificationsForProcessing(Pageable pageable);

    // 24시간 동안 안 읽은(SENT 상태) 알림 찾기
    List<Notification> findByStatusAndIsReadFalseAndCreatedAtBefore(
            NotificationStatus status, LocalDateTime dateTime);

    // 특정 사용자의 모든 알림을 한 번에 읽음 처리
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Notification n SET n.isRead = true " +
            "WHERE n.recipient = :user AND n.isRead = false")
    int markAllAsRead(@Param("user") User user);
}