package com.example.carmmunity_capstone.dto;

import com.example.carmmunity_capstone.entity.NotificationType;
import com.example.carmmunity_capstone.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 팔로우 또는 팔로우 수락 이벤트 발생 시 전파되는 이벤트 객체
 */
@Getter
@RequiredArgsConstructor
public class FollowEvent {

    // 알림을 유발한 사용자 (알림 팝업에 표시될)
    private final User sender;

    // 알림을 받을 사용자 (토픽 구독자)
    private final User receiver;

    // 알림 메시지
    private final String message;

    // 알림 유형
    private final NotificationType type;
}