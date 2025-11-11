package com.example.carmmunity_capstone.entity;

/**
 * '팔로우' 모델의 관계 상태를 정의하는 Enum
 */
public enum FriendshipStatus {

    /**
     * 팔로우 중인 상태. (공개 계정을 팔로우했거나, 비공개 계정 요청이 수락됨)
     */
    FOLLOWING,

    /**
     * 비공개 계정에 팔로우를 요청하고 대기 중인 상태.
     */
    PENDING,

    /**
     * 비공개 계정 주인이 팔로우 요청을 거절한 상태.
     */
    DECLINED,

    /**
     * 특정 사용자를 차단한 상태. (A가 B를 차단함)
     * 이 관계는 비대칭적입니다.
     */
    BLOCKED
}