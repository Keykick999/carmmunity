package com.example.carmmunity_capstone.domain;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer commentId;

    @Lob
    @Column(nullable = false)
    private String commentContent;

    private LocalDateTime deletedAt;

    @Column(nullable = false)
    private Integer totalLike = 0;

    @ManyToOne
    @JoinColumn(name = "postId", insertable = false, updatable = false)
    private Post post;

    @ManyToOne
    @JoinColumn(name = "userId", insertable = false, updatable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "parentCommentId", insertable = false, updatable = false)
    private Comment parentComment;

    @ManyToOne
    @JoinColumn(name = "mentionUserId", insertable = false, updatable = false)
    private User mentionedUser;
}
