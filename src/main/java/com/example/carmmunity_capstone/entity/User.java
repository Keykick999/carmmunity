package com.example.carmmunity_capstone.entity;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

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
    @Column(name = "User_ID", length = 50)
    private String userId;

    @Column(name = "Nickname", nullable = false, length = 50)
    private String nickname;

    @Column(name = "Password", nullable = false, length = 255)
    private String password;

    @Column(name = "Email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "Phone", length = 20)
    private String phone;

    @Column(name = "Name", nullable = false, length = 50)
    private String name;

    @Column(name = "Introduction", length = 1000)
    private String introduction;

    @Column(name = "Profile_Image_Path")
    private String profileImagePath;

    @CreatedDate
    @Column(name = "Created_At")
    private LocalDateTime createdAt;
}
