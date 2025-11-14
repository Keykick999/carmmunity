package com.example.carmmunity_capstone.entity;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;
import java.util.UUID;

@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "User")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @Column(name = "user_id", length = 50)
    private String userId; // 로그인 ID (내부용)

    /**
     * [보안 강화] API 엔드포인트에 노출될 예측 불가능한 고유 식별자
     * 이 값으로 API 요청을 받습니다.
     */
    @Column(name = "user_uuid", nullable = false, unique = true, updatable = false)
    private String userUuid;

    @Column(name = "nickname", nullable = false, length = 50)
    private String nickname;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "introduction", length = 1000)
    private String introduction;

    @Column(name = "profile_image_path")
    private String profileImagePath;

    @Column(name = "is_private", nullable = false)
    private boolean isPrivate = false; // 기본값은 공개(false)

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * 엔티티가 처음 저장되기 직전에 호출되어 UUID를 생성합니다.
     * (회원가입 시 한 번만 실행됨)
     */
    @PrePersist
    public void prePersist() {
        if (this.userUuid == null) {
            this.userUuid = UUID.randomUUID().toString();
        }
    }
}