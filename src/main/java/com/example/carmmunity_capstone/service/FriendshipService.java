package com.example.carmmunity_capstone.service;

import com.example.carmmunity_capstone.dto.UserSummaryDto;
import com.example.carmmunity_capstone.entity.Friendship;
import com.example.carmmunity_capstone.entity.FriendshipStatus;
import com.example.carmmunity_capstone.entity.User;
import com.example.carmmunity_capstone.repository.FriendshipRepository;
import com.example.carmmunity_capstone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    /**
     * 1. 팔로우하기 (A가 B를 팔로우)
     * - B가 공개 계정: (A -> B, FOLLOWING) 즉시 생성
     * - B가 비공개 계정: (A -> B, PENDING) 생성
     */
    public void followUser(String followerUuid, String followeeUuid) {
        User follower = findUserByUuid(followerUuid); // A (팔로우 거는 사람)
        User followee = findUserByUuid(followeeUuid); // B (팔로우 당하는 사람)

        if (followerUuid.equals(followeeUuid)) {
            throw new RuntimeException("자신을 팔로우할 수 없습니다.");
        }

        // B가 A를 차단했는지, A가 B를 차단했는지 확인
        checkBlockStatus(follower, followee);

        // B가 비공개 계정인지 확인
        FriendshipStatus newStatus = followee.isPrivate() ?
                FriendshipStatus.PENDING :
                FriendshipStatus.FOLLOWING;

        // (A -> B) 관계를 찾음
        Friendship friendship = friendshipRepository.findByRequesterAndReceiver(follower, followee)
                .orElse(
                        Friendship.builder() // 관계가 없으면 새로 생성
                                .requester(follower)
                                .receiver(followee)
                                .build()
                );

        // 기존 관계에 따른 분기 처리 (switch가 확장성에 용이)
        switch (friendship.getStatus() != null ? friendship.getStatus() : FriendshipStatus.DECLINED) { // 편의상 null은 DECLINED 취급
            case FOLLOWING:
                throw new RuntimeException("이미 팔로우 중입니다.");
            case PENDING:
                throw new RuntimeException("이미 팔로우 요청을 보낸 상태입니다.");
            case DECLINED:
                // 거절됐던 상태면, 새 상태(PENDING 또는 FOLLOWING)로 업데이트
                friendship.setStatus(newStatus);
                friendshipRepository.save(friendship);
                break;
            case BLOCKED:
                // blockUser에서 (A->B, BLOCKED)를 만들었으므로 이 경우는 checkBlockStatus에서 걸림
                throw new RuntimeException("차단한 사용자입니다.");
        }
    }

    /**
     * 2. 언팔로우 / 팔로우 요청 취소 (A가 B를)
     * (A -> B, FOLLOWING) 또는 (A -> B, PENDING) 레코드를 삭제합니다.
     */
    public void unfollowUser(String followerUuid, String followeeUuid) {
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
        // "역방향" 레코드는 절대 생성하지 않습니다.
    }

    /**
     * 4. 팔로우 요청 거절 (A가 B의 요청을 거절)
     * (B -> A, PENDING) 레코드를 (B -> A, DECLINED)로 변경합니다.
     */
    public void declineFollowRequest(String receiverUuid, String requesterUuid) {
        User receiver = findUserByUuid(receiverUuid);   // A (나)
        User requester = findUserByUuid(requesterUuid); // B (상대방)

        Friendship request = friendshipRepository.findByRequesterAndReceiver(requester, receiver)
                .filter(f -> f.getStatus() == FriendshipStatus.PENDING)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 팔로우 요청입니다."));

        request.setStatus(FriendshipStatus.DECLINED);
        friendshipRepository.save(request);
    }

    /**
     * 5. 팔로워 삭제하기 (A가 나를 팔로우하는 B를 삭제)
     * (B -> A, FOLLOWING) 레코드를 삭제합니다.
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
     * (A -> B, BLOCKED) 레코드를 생성/업데이트합니다.
     * (B -> A) 관계는 님 말씀대로 절대 건드리지 않습니다.
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
     * (A -> B, BLOCKED) 레코드를 삭제합니다.
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
    public List<UserSummaryDto> searchUsers(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            return List.of();
        }
        return userRepository.findByNicknameContainingIgnoreCase(nickname)
                .stream()
                .map(UserSummaryDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 9. 팔로잉 목록 조회 (targetUserUuid가 팔로우하는 사람들)
     */
    @Transactional(readOnly = true)
    public List<UserSummaryDto> getFollowingList(String currentUserUuid, String targetUserUuid) {
        User targetUser = findUserByUuid(targetUserUuid);

        // 권한 검사
        checkReadPermission(currentUserUuid, targetUser);

        // (검사 통과) 로직 실행
        List<Friendship> relationships = friendshipRepository.findByRequesterAndStatus(
                targetUser, FriendshipStatus.FOLLOWING
        );

        return relationships.stream()
                .map(Friendship::getReceiver)
                .map(UserSummaryDto::from)
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
}