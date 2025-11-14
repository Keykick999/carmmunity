package com.example.carmmunity_capstone.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailVerification {

    @Id
    @Column(name = "Email", nullable = false, length = 255)
    private String email;

    @Column(name = "user_uuid", length = 50)
    private String userId;

    @Column(name = "Verification_Code", nullable = false, length = 10)
    private String verificationCode;

    @Column(name = "Purpose", nullable = false, length = 10)
    private String purpose; // "signup" or "reset"

    @Column(name = "Created_At", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "Is_Verified", nullable = false)
    private Boolean isVerified = false;

    public boolean isExpired() {
        return createdAt.plusMinutes(3).isBefore(LocalDateTime.now());
    }
}
