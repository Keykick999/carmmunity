package com.example.carmmunity_capstone.dto;

import com.example.carmmunity_capstone.entity.RelationshipStatus; // Service 코드에서 이 패키지를 사용하고 있으므로 import
import com.example.carmmunity_capstone.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder // @AllArgsConstructor 대신 @Builder를 사용하여 유연한 객체 생성
public class UserSummaryDto {
    private String userUuid;
    private String nickname;
    private String profileImagePath;
    // 현재 로그인된 유저와 이 목록 유저 간의 관계 상태
    private RelationshipStatus relationshipStatus;
    @Builder.Default // 기본값 0
    private int unreadMessageCount = 0;

    /**
     * User 엔티티를 UserSummaryDto로 변환하는 정적 팩토리 메서드 (기본 정보만)
     */
    public static UserSummaryDto from(User user) {
        return UserSummaryDto.builder()
                .userUuid(user.getUserUuid())
                .nickname(user.getNickname())
                .profileImagePath(user.getProfileImagePath())
                // 관계 상태가 필요 없는 목록(예: 받은 요청 목록)에서 사용될 수 있습니다.
                .relationshipStatus(RelationshipStatus.NOT_FOLLOWING)
                .build();
    }

    /**
     * User 엔티티와 관계 상태를 포함하여 DTO를 생성하는 팩토리 메서드 (검색/팔로잉 목록용)
     */
    public static UserSummaryDto from(User user, RelationshipStatus status) {
        return UserSummaryDto.builder()
                .userUuid(user.getUserUuid())
                .nickname(user.getNickname())
                .profileImagePath(user.getProfileImagePath())
                .relationshipStatus(status) // 관계 상태 설정
                .build();
    }

    public static UserSummaryDto from(User user, int unreadMessageCount) {
        return UserSummaryDto.builder()
                .userUuid(user.getUserUuid())
                .nickname(user.getNickname())
                .profileImagePath(user.getProfileImagePath())
                .unreadMessageCount(unreadMessageCount)
                .build();
    }
}