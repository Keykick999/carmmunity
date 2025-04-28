package com.example.carmmunity_capstone.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


/**
 * 게시글 저장 및 수정을 위한 공통 요청 DTO입니다.
 *
 * 사용 API:
 * - 게시글 작성 API
 * - 게시글 수정 API
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class PostRequest {
    private String title;
    private String content;
    private PostType postType;
}
