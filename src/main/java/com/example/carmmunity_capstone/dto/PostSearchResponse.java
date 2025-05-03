package com.example.carmmunity_capstone.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Setter
@Getter
@AllArgsConstructor
public class PostSearchResponse {
    private Integer postId;

    private String postTitle;

    private PostType postType;

    private String postContent;

    private String imagePath;

    private Integer totalLike = 0;

    private String nickname;

    private LocalDateTime updatedAt;
}