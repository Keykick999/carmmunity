package com.example.carmmunity_capstone.repository;

import com.example.carmmunity_capstone.entity.Friendship;
import com.example.carmmunity_capstone.entity.FriendshipStatus;
import com.example.carmmunity_capstone.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    // 단일 관계 조회 (상태 변경용)
    Optional<Friendship> findByRequesterAndReceiver(User requester, User receiver);

    /**
     * 팔로잉 목록 조회용 (A가 팔로우하는 사람들)
     * A = Requester
     */
    List<Friendship> findByRequesterAndStatus(User requester, FriendshipStatus status);

    /**
     * 팔로워/받은 요청 목록 조회용 (A를 팔로우하는 사람들)
     * A = Receiver
     */
    List<Friendship> findByReceiverAndStatus(User receiver, FriendshipStatus status);
}