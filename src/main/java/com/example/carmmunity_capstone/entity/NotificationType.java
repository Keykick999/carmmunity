package com.example.carmmunity_capstone.entity;

/**
 * 알림의 종류를 구분하기 위한 Enum
 * (Notification 엔티티에서 사용)
 */
public enum NotificationType {

    // 팔로우 관련
    NEW_FOLLOWER,      // 새로운 팔로워 (공개 계정)
    FOLLOW_REQUEST,    // 팔로우 요청 (비공개 계정)
    FOLLOW_ACCEPTED,   // 내 팔로우 요청이 수락됨

    // 메시지 관련
    NEW_MESSAGE        // 새 메시지 수신 (채팅방 닫혀있을 때)
}