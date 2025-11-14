package com.example.carmmunity_capstone.repository;

import com.example.carmmunity_capstone.entity.UserTerm;
import com.example.carmmunity_capstone.entity.UserTermId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserTermRepository extends JpaRepository<UserTerm, UserTermId> {

    // 특정 유저의 모든 약관 동의 내역
    List<UserTerm> findByUserUuid(String userUuid);

    // 특정 유저-약관 조합으로 단일 동의 내역 조회
    Optional<UserTerm> findByUserUuidAndTermCode(String userUuid, String termCode);
}
