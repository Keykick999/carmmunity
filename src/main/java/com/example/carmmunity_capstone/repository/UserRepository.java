package com.example.carmmunity_capstone.repository;

import com.example.carmmunity_capstone.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findIdByEmail(String email);

    Optional<User> findByUserUuid(String userUuid);

    List<User> findByNicknameContainingIgnoreCaseAndUserUuidNot(String nickname, String excludeUserUuid);

    // 자체 로그인 ID로 찾는 메서드 (이미 있다면 그대로 두세요)
    Optional<User> findByUserId(String userId);
}
