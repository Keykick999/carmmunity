package com.example.carmmunity_capstone.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;


@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "postId")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_uuid")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parentCommentId")
    private Comment parentComment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentionUserId")
    private User mentionedUser;
}
