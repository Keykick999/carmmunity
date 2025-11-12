package com.example.carmmunity_capstone.controller;

import com.example.carmmunity_capstone.dto.UserSummaryDto;
import com.example.carmmunity_capstone.service.FriendshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class FriendshipController {

    private final FriendshipService friendshipService;

    /**
     * 1. 팔로우하기
     * - (A)가 (B)를 팔로우
     */
    @PostMapping("/follow/{followeeUuid}")
    public ResponseEntity<Void> followUser(
            @AuthenticationPrincipal String currentUserUuid, // A (Follower)
            @PathVariable String followeeUuid) { // B (Followee)
        friendshipService.followUser(currentUserUuid, followeeUuid);
        return ResponseEntity.ok().build();
    }

    /**
     * 2. 언팔로우 / 팔로우 요청 취소
     * - (A)가 (B)를 언팔로우 (또는 보낸 요청 취소)
     */
    @DeleteMapping("/follow/{followeeUuid}")
    public ResponseEntity<Void> unfollowUser(
            @AuthenticationPrincipal String currentUserUuid, // A (Follower)
            @PathVariable String followeeUuid) { // B (Followee)

        friendshipService.unfollowUser(currentUserUuid, followeeUuid);
        return ResponseEntity.ok().build();
    }

    /**
     * 3. 팔로우 요청 수락
     * - (A)가 (B)의 팔로우 요청을 수락
     */
    @PostMapping("/accept/{requesterUuid}")
    public ResponseEntity<Void> acceptFollowRequest(
            @AuthenticationPrincipal String currentUserUuid, // A (Receiver)
            @PathVariable String requesterUuid) { // B (Requester)

        friendshipService.acceptFollowRequest(currentUserUuid, requesterUuid);
        return ResponseEntity.ok().build();
    }

    /**
     * 4. 팔로우 요청 거절
     * - (A)가 (B)의 팔로우 요청을 거절
     */
    @PostMapping("/decline/{requesterUuid}")
    public ResponseEntity<Void> declineFollowRequest(
            @AuthenticationPrincipal String currentUserUuid, // A (Receiver)
            @PathVariable String requesterUuid) { // B (Requester)

        friendshipService.declineFollowRequest(currentUserUuid, requesterUuid);
        return ResponseEntity.ok().build();
    }

    /**
     * 5. 팔로워 삭제하기
     * - (A)가 나를 팔로우하는 (B)를 삭제
     */
    @DeleteMapping("/follower/{followerUuid}")
    public ResponseEntity<Void> removeFollower(
            @AuthenticationPrincipal String currentUserUuid, // A (User)
            @PathVariable String followerUuid) { // B (Follower)

        friendshipService.removeFollower(currentUserUuid, followerUuid);
        return ResponseEntity.ok().build();
    }

    /**
     * 6. 사용자 차단하기
     * - (A)가 (B)를 차단
     */
    @PostMapping("/block/{blockedUuid}")
    public ResponseEntity<Void> blockUser(
            @AuthenticationPrincipal String currentUserUuid, // A (Blocker)
            @PathVariable String blockedUuid) { // B (Blocked)

        friendshipService.blockUser(currentUserUuid, blockedUuid);
        return ResponseEntity.ok().build();
    }

    /**
     * 7. 사용자 차단 해제하기
     * - (A)가 (B) 차단을 해제
     */
    @DeleteMapping("/block/{blockedUuid}")
    public ResponseEntity<Void> unblockUser(
            @AuthenticationPrincipal String currentUserUuid, // A (Blocker)
            @PathVariable String blockedUuid) { // B (Blocked)

        friendshipService.unblockUser(currentUserUuid, blockedUuid);
        return ResponseEntity.ok().build();
    }



    /**
     * 8. 전체 사용자 검색
     */
    @GetMapping("/search")
    public ResponseEntity<List<UserSummaryDto>> searchUsers(
            @AuthenticationPrincipal String currentUserUuid,
            @RequestParam("nickname") String nickname) {
        return ResponseEntity.ok(friendshipService.searchUsers(currentUserUuid, nickname));
    }

    /**
     * 9-1. 현재 로그인된 유저의 팔로잉 목록 조회 (토큰 기반)
     */
    @GetMapping("/following")
    public ResponseEntity<List<UserSummaryDto>> getMyFollowingList(
            @AuthenticationPrincipal String currentUserUuid) { // 현재 로그인한 사용자 UUID
        return ResponseEntity.ok(friendshipService.getFollowingList(currentUserUuid, currentUserUuid));
    }

    /**
     * 9. 팔로잉 목록 조회 (특정 유저의)
     */
    @GetMapping("/{targetUserUuid}/following")
    public ResponseEntity<List<UserSummaryDto>> getFollowingList(
            @AuthenticationPrincipal String currentUserUuid, // (주체) 요청한 사람
            @PathVariable String targetUserUuid) { // (대상) 보려는 프로필
        return ResponseEntity.ok(friendshipService.getFollowingList(currentUserUuid, targetUserUuid));
    }

    /**
     * 10. 팔로워 목록 조회 (특정 유저의)
     */
    @GetMapping("/{targetUserUuid}/followers")
    public ResponseEntity<List<UserSummaryDto>> getFollowersList(
            @AuthenticationPrincipal String currentUserUuid, // (주체) 요청한 사람
            @PathVariable String targetUserUuid) {

        return ResponseEntity.ok(friendshipService.getFollowersList(currentUserUuid, targetUserUuid));
    }

    /**
     * 11. 받은 팔로우 요청 목록 조회
     */
    @GetMapping("/requests/received")
    public ResponseEntity<List<UserSummaryDto>> getReceivedFollowRequests(
            @AuthenticationPrincipal String currentUserUuid) {
        return ResponseEntity.ok(friendshipService.getReceivedFollowRequests(currentUserUuid));
    }


    // --- 예외 처리 ---
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}