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
public class WithdrawnUser {

    @Id
    @Column(name = "user_uuid", length = 50)
    private String userId;

    @Column(name = "Deleted_At")
    private LocalDateTime deletedAt = LocalDateTime.now();
}