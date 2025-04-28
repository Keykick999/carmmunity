package com.example.carmmunity_capstone.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "USER")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Id
    @Column(name = "User_ID")
    private String id;

    @Column(name = "Nickname", length = 500, nullable = false)
    private String nickname;

    @Column(name = "Password", length = 255, nullable = false)
    private String password;

    @Column(name = "Email", length = 255, nullable = false)
    private String email;

    @Column(name = "Phone", length = 20)
    private String phone;

    @Column(name = "Name", length = 100)
    private String name;
}
