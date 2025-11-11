package com.example.carmmunity_capstone.dto; // 경로는 알맞게 수정하세요

import com.example.carmmunity_capstone.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserSummaryDto {
    private String userUuid;
    private String nickname;
    private String profileImagePath;

    /**
     * User 엔티티를 UserSummaryDto로 변환하는 정적 팩토리 메서드
     */
    public static UserSummaryDto from(User user) {
        return new UserSummaryDto(
                user.getUserUuid(),
                user.getNickname(),
                user.getProfileImagePath()
        );
    }
}