package com.example.carmmunity_capstone.entity;

/**
 * 목록의 대상 유저와 현재 로그인 유저 간의 관계 상태
 */
public enum RelationshipStatus {
    /** 현재 유저가 이 대상을 팔로우하지 않고 있음 */
    NOT_FOLLOWING,
    /** 현재 유저가 이 대상을 이미 팔로우하고 있음 */
    FOLLOWING,
    /** 현재 유저가 이 대상에게 팔로우 요청을 보냈고 대기 중임 */
    REQUESTED,
    /** 이 대상이 현재 유저를 차단했거나, 현재 유저가 이 대상을 차단했음 */
    BLOCKED
}