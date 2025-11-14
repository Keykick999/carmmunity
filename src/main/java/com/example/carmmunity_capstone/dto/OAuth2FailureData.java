package com.example.carmmunity_capstone.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;

/**
 * 이메일 미제공 시 추가 가입 페이지로 전달할 필수 데이터를 담는 DTO
 */
@Getter
@AllArgsConstructor
@ToString
public class OAuth2FailureData implements Serializable {
    private final String providerId;
    private final String provider;
    private final String nickname;
    private final String picture; // 프로필 이미지 경로
}