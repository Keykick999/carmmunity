package com.example.carmmunity_capstone.entity;

import com.example.carmmunity_capstone.dto.PostType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer postId;

    @Column(nullable = false, length = 100)
    private String postTitle;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PostType postType;

    @Lob
    @Column(nullable = false)
    private String postContent;

    @Column(nullable = true)
    private String imagePath;

    @Column
    private String thumbnailPath;

    private LocalDateTime deletedAt;

    @Column(nullable = false)
    private Integer totalCount = 0;

    @Column(nullable = false)
    private Integer totalLike = 0;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false)
    private User user;
}
