package com.example.carmmunity_capstone.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "UserCar")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserCar {

    @EmbeddedId
    private UserCarId id;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "User_ID", nullable = false)
    private User user;

    @Column(name = "Image_Path")
    private String imagePath; // 서버에 저장된 glTF 파일 경로

    @Column(name = "Created_At", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "Updated_At")
    private LocalDateTime updatedAt;

    @Column(name = "Deleted_At")
    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}








