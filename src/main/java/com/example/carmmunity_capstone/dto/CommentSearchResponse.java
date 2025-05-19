package com.example.carmmunity_capstone.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
@AllArgsConstructor
public class CommentSearchResponse {
    private Integer commentId;
    private String commentContent;
    private Integer totalLike = 0;
    private boolean isAuthor;
    private String nickName;
}