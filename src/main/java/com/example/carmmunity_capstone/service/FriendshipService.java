package com.example.carmmunity_capstone.service;

import com.example.carmmunity_capstone.dto.FollowEvent;
import com.example.carmmunity_capstone.dto.NotificationDto; // DTO 임포트
import com.example.carmmunity_capstone.dto.UserSummaryDto;
import com.example.carmmunity_capstone.entity.*;
import com.example.carmmunity_capstone.repository.FriendshipRepository;
import com.example.carmmunity_capstone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // Log 임포트
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j // 1. 로그 추가
public class FriendshipService {

    private final ApplicationEventPublisher eventPublisher;

    // 2. SimpMessagingTemplate 주입 (RequiredArgsConstructor가 처리)
    private final NotificationService notificationService;

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final ChatService chatService;

    /**
     * 1. 팔로우하기 (A가 B를 팔로우)
     * - B가 공개 계정: (A -> B, FOLLOWING) 즉시 생성
     * - B가 비공개 계정: (A -> B, PENDING) 생성
    rm */
    public void followUser(String followerUuid, String followeeUuid) {
        User follower = findUserByUuid(followerUuid); // A (팔로우 거는 사람)
        User followee = findUserByUuid(followeeUuid); // B (팔로우 당하는 사람)

        if (followerUuid.equals(followeeUuid)) {
            throw new RuntimeException("자신을 팔로우할 수 없습니다.");
        }

        checkBlockStatus(follower, followee);

        FriendshipStatus newStatus = followee.isPrivate() ?
                FriendshipStatus.PENDING :
                FriendshipStatus.FOLLOWING;

        Friendship friendship = friendshipRepository.findByRequesterAndReceiver(follower, followee)
                .orElse(
                        Friendship.builder()
                                .requester(follower)
                                .receiver(followee)
                                .build()
                );

        switch (friendship.getStatus() != null ? friendship.getStatus() : FriendshipStatus.DECLINED) {
            case FOLLOWING:
                throw new RuntimeException("이미 팔로우 중입니다.");
            case PENDING:
                throw new RuntimeException("이미 팔로우 요청을 보낸 상태입니다.");
            case DECLINED:
                friendship.setStatus(newStatus);
                friendshipRepository.save(friendship);

                // --- [수정] 알림 로직 -> 이벤트 발행 ---
                String message;
                NotificationType type;

                if (newStatus == FriendshipStatus.PENDING) {
                    message = follower.getNickname() + "님이 회원님에게 팔로우 요청을 보냈습니다.";
                    type = NotificationType.FOLLOW_REQUEST;
                } else {
                    message = follower.getNickname() + "님이 회원님을 팔로우합니다.";
                    type = NotificationType.NEW_FOLLOWER;
                }

                // [수정] NotificationService 직접 호출 대신 이벤트 발행
                eventPublisher.publishEvent(new FollowEvent(follower, followee, message, type));
                // --- 알림 로직 끝 ---

                break;
            case BLOCKED:
                throw new RuntimeException("차단한 사용자입니다.");
        }
    }

    /**
     * 2. 언팔로우 / 팔로우 요청 취소 (A가 B를)
     * (A -> B, FOLLOWING) 또는 (A -> B, PENDING) 레코드를 삭제합니다.
     */
    public void unfollowUser(String followerUuid, String followeeUuid) {
        // (기존 로직 ...)
        User follower = findUserByUuid(followerUuid); // A
        User followee = findUserByUuid(followeeUuid); // B

        Friendship friendship = friendshipRepository.findByRequesterAndReceiver(follower, followee)
                .filter(f -> f.getStatus() == FriendshipStatus.FOLLOWING ||
                        f.getStatus() == FriendshipStatus.PENDING)
                .orElseThrow(() -> new RuntimeException("팔로우/요청 중인 상태가 아닙니다."));

        friendshipRepository.delete(friendship);
    }

    /**
     * 3. 팔로우 요청 수락 (A가 B의 요청을 수락)
     * (A = 비공개 계정 주인, B = 요청자)
     * (B -> A, PENDING) 레코드를 (B -> A, FOLLOWING)으로 변경합니다.
     */
    public void acceptFollowRequest(String receiverUuid, String requesterUuid) {
        User receiver = findUserByUuid(receiverUuid);   // A (나)
        User requester = findUserByUuid(requesterUuid); // B (상대방)

        Friendship request = friendshipRepository.findByRequesterAndReceiver(requester, receiver)
                .filter(f -> f.getStatus() == FriendshipStatus.PENDING)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 팔로우 요청입니다."));

        request.setStatus(FriendshipStatus.FOLLOWING);
        friendshipRepository.save(request);

        // --- [수정] 알림 로직 -> 이벤트 발행 ---
        String message = receiver.getNickname() + "님이 팔로우 요청을 수락했습니다.";

        // [수정] NotificationService 직접 호출 대신 이벤트 발행
        // (알림 유발자: receiver, 알림 수신자: requester)
        eventPublisher.publishEvent(new FollowEvent(
                receiver,
                requester,
                message,
                NotificationType.FOLLOW_ACCEPTED
        ));
        // --- 알림 로직 끝 ---
    }

    /**
     * 4. 팔로우 요청 거절 (A가 B의 요청을 거절)
     * (B -> A, PENDING) 레코드를 (B -> A, DECLINED)로 변경합니다.
     */
    public void declineFollowRequest(String receiverUuid, String requesterUuid) {
        // (기존 로직 ...)
        User receiver = findUserByUuid(receiverUuid);   // A (나)
        User requester = findUserByUuid(requesterUuid); // B (상대방)

        Friendship request = friendshipRepository.findByRequesterAndReceiver(requester, receiver)
                .filter(f -> f.getStatus() == FriendshipStatus.PENDING)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 팔로우 요청입니다."));

        request.setStatus(FriendshipStatus.DECLINED);
        friendshipRepository.save(request);
    }

    // ... (removeFollower, blockUser, unblockUser 등 나머지 메서드는 그대로) ...
    // (기존 코드 생략)

    /**
     * 5. 팔로워 삭제하기 (A가 나를 팔로우하는 B를 삭제)
     */
    public void removeFollower(String userUuid, String followerUuid) {
        User user = findUserByUuid(userUuid);         // A (나)
        User follower = findUserByUuid(followerUuid); // B (나를 팔로우하는 사람)

        Friendship friendship = friendshipRepository.findByRequesterAndReceiver(follower, user)
                .filter(f -> f.getStatus() == FriendshipStatus.FOLLOWING)
                .orElseThrow(() -> new RuntimeException("상대방이 나를 팔로우하고 있지 않습니다."));

        friendshipRepository.delete(friendship);
    }

    /**
     * 6. 사용자 차단하기 (A가 B를 차단)
     */
    public void blockUser(String blockerUuid, String blockedUuid) {
        User blocker = findUserByUuid(blockerUuid); // A
        User blocked = findUserByUuid(blockedUuid); // B

        Friendship blockRelation = friendshipRepository.findByRequesterAndReceiver(blocker, blocked)
                .orElse(
                        Friendship.builder()
                                .requester(blocker)
                                .receiver(blocked)
                                .build()
                );
        blockRelation.setStatus(FriendshipStatus.BLOCKED);
        friendshipRepository.save(blockRelation);
    }

    /**
     * 7. 사용자 차단 해제하기 (A가 B를 차단 해제)
     */
    public void unblockUser(String blockerUuid, String blockedUuid) {
        User blocker = findUserByUuid(blockerUuid);
        User blocked = findUserByUuid(blockedUuid);

        Friendship blockRelation = friendshipRepository.findByRequesterAndReceiver(blocker, blocked)
                .filter(f -> f.getStatus() == FriendshipStatus.BLOCKED)
                .orElseThrow(() -> new RuntimeException("차단한 상태의 사용자가 아닙니다."));

        friendshipRepository.delete(blockRelation);
    }

    /**
     * 8. 전체 사용자 검색
     */
    @Transactional(readOnly = true)
    public List<UserSummaryDto> searchUsers(String currentUserUuid, String nickname) {
        if (nickname == null || nickname.isBlank()) {
            return List.of();
        }

        // 1. 현재 로그인 사용자 엔티티 조회
        User currentUser = findUserByUuid(currentUserUuid);

        // 2. 닉네임으로 사용자 검색
        return userRepository.findByNicknameContainingIgnoreCaseAndUserUuidNot(nickname, currentUserUuid)
                .stream()
                .map(searchedUser -> {
                    // 3. 현재 사용자와 검색된 사용자 간의 관계 상태 결정
                    RelationshipStatus status = determineRelationshipStatus(currentUser, searchedUser);

                    // 4. 관계 상태를 포함하여 DTO 생성
                    return UserSummaryDto.from(searchedUser, status);
                })
                .collect(Collectors.toList());
    }

    /**
     * 9. 팔로잉 목록 조회 (targetUserUuid가 팔로우하는 사람들)
     */
    @Transactional(readOnly = true)
    public List<UserSummaryDto> getFollowingList(String currentUserUuid, String targetUserUuid) {

        // 현재 로그인한 유저를 찾습니다. (안 읽음 카운트 조회의 주체)
        User currentUser = findUserByUuid(currentUserUuid);

        User targetUser = findUserByUuid(targetUserUuid);

        checkReadPermission(currentUserUuid, targetUser);

        List<Friendship> relationships = friendshipRepository.findByRequesterAndStatus(
                targetUser, FriendshipStatus.FOLLOWING
        );

        // DTO를 만들 때 ChatService를 통해 안 읽음 카운트를 계산합니다.
        return relationships.stream()
                .map(Friendship::getReceiver) // (followee: 내가 팔로우하는 사람)
                .map(followee -> {

                    int unreadCount = 0;
                    // "내가 내 팔로잉 목록을 볼 때만" 안 읽음 카운트를 계산합니다.
                    if (currentUserUuid.equals(targetUserUuid)) {
                        // 4단계에서 만든 ChatService의 메서드 호출
                        unreadCount = chatService.getUnreadMessageCount(currentUser, followee);
                    }

                    // 1단계에서 수정한 'from' 메서드를 사용합니다.
                    return UserSummaryDto.from(followee, unreadCount);
                })
                .collect(Collectors.toList());
    }

    /**
     * 10. 팔로워 목록 조회 (targetUserUuid를 팔로우하는 사람들)
     */
    @Transactional(readOnly = true)
    public List<UserSummaryDto> getFollowersList(String currentUserUuid, String targetUserUuid) {
        User targetUser = findUserByUuid(targetUserUuid);

        // 권한 검사
        checkReadPermission(currentUserUuid, targetUser);

        // (검사 통과) 로직 실행
        List<Friendship> relationships = friendshipRepository.findByReceiverAndStatus(
                targetUser, FriendshipStatus.FOLLOWING
        );

        return relationships.stream()
                .map(Friendship::getRequester)
                .map(UserSummaryDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 11. 받은 팔로우 요청 목록 조회
     */
    @Transactional(readOnly = true)
    public List<UserSummaryDto> getReceivedFollowRequests(String currentUserUuid) {
        User currentUser = findUserByUuid(currentUserUuid);
        List<Friendship> relationships = friendshipRepository.findByReceiverAndStatus(
                currentUser, FriendshipStatus.PENDING
        );


        return relationships.stream()
                .map(Friendship::getRequester)
                .map(UserSummaryDto::from)
                .collect(Collectors.toList());
    }


    // --- Helper Methods ---
    /**
     *
     * @param currentUserUuid 요청자
     * @param targetUser      조회 대상
     */
    private void checkReadPermission(String currentUserUuid, User targetUser) {
        // 1. 대상이 비공개 계정이 아니면 -> 누구나 통과
        if (!targetUser.isPrivate()) {
            return;
        }

        // 2. 대상이 비공개 계정인 경우
        // 2-1. 요청자가 본인이면 -> 통과
        if (currentUserUuid.equals(targetUser.getUserUuid())) {
            return;
        }

        // 2-2. 요청자가 본인이 아니면, 요청자가 대상을 팔로우 중인지 확인
        User currentUser = findUserByUuid(currentUserUuid);
        boolean isFollowing = friendshipRepository.findByRequesterAndReceiver(currentUser, targetUser)
                .map(f -> f.getStatus() == FriendshipStatus.FOLLOWING)
                .orElse(false);

        if (!isFollowing) {
            // 팔로우 중이 아니면 -> 권한 없음
            throw new RuntimeException("비공개 계정의 목록을 볼 권한이 없습니다.");
        }
        // (팔로우 중이면 통과)
    }

    private User findUserByUuid(String userUuid) {
        return userRepository.findByUserUuid(userUuid)
                .orElseThrow(() -> new RuntimeException("User not found with uuid: " + userUuid));
    }

    private void checkBlockStatus(User userA, User userB) {
        // ... (기존 차단 검사 로직)
        if (friendshipRepository.findByRequesterAndReceiver(userA, userB).map(f -> f.getStatus() == FriendshipStatus.BLOCKED).orElse(false)) {
            throw new RuntimeException("이미 차단한 사용자입니다.");
        }
        if (friendshipRepository.findByRequesterAndReceiver(userB, userA).map(f -> f.getStatus() == FriendshipStatus.BLOCKED).orElse(false)) {
            throw new RuntimeException("상대방에 의해 차단된 상태입니다.");
        }
    }

    /**
     * 현재 로그인된 사용자(requester)와 목록의 대상(receiver) 간의 관계 상태를 결정합니다.
     * @param requester 현재 로그인된 사용자
     * @param receiver 목록에 표시될 사용자
     * @return RelationshipStatus
     */
    private RelationshipStatus determineRelationshipStatus(User requester, User receiver) {
        // 1. 요청자가 자기 자신인 경우 (자기 목록을 볼 때)
        if (requester.getUserUuid().equals(receiver.getUserUuid())) {
            return RelationshipStatus.FOLLOWING; // 자기 자신은 팔로우 상태로 간주 (UI 처리용)
        }

        // 2. 차단 상태 확인 (양방향)
        // requester가 receiver를 차단했거나, receiver가 requester를 차단했는지 확인
        if (friendshipRepository.findByRequesterAndReceiver(requester, receiver)
                .filter(f -> f.getStatus() == FriendshipStatus.BLOCKED).isPresent() ||
                friendshipRepository.findByRequesterAndReceiver(receiver, requester)
                        .filter(f -> f.getStatus() == FriendshipStatus.BLOCKED).isPresent()) {
            return RelationshipStatus.BLOCKED;
        }

        // 3. 팔로우 상태 확인 (requester가 receiver를 팔로우하고 있는지)
        return friendshipRepository.findByRequesterAndReceiver(requester, receiver)
                .map(friendship -> {
                    if (friendship.getStatus() == FriendshipStatus.FOLLOWING) {
                        return RelationshipStatus.FOLLOWING;
                    } else if (friendship.getStatus() == FriendshipStatus.PENDING) {
                        return RelationshipStatus.REQUESTED;
                    }
                    return RelationshipStatus.NOT_FOLLOWING;
                })
                .orElse(RelationshipStatus.NOT_FOLLOWING);
    }
}