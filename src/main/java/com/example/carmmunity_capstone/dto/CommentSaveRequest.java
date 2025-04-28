package com.example.carmmunity_capstone.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CommentSaveRequest {
    private String content;
    private Integer postId;
    private Integer parentCommentId;
    private String mentionUserId;
}
