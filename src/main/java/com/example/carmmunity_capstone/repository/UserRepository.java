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
}
