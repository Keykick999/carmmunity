package com.example.carmmunity_capstone.dto;

import com.example.carmmunity_capstone.domain.User;
import lombok.Setter;


@Setter
public class CommentSearchRequest {
    private Integer commentId;
    private String commentContent;
    private Integer totalLike = 0;
    private boolean isAuthor;
    // TODO 언급한 회원의 마이페이지 url이랑 닉네임을 찾아서 포함시키기
    private User mentionedUser;
}
