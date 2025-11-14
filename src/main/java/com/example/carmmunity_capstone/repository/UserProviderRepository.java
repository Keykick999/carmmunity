package com.example.carmmunity_capstone.repository;

import com.example.carmmunity_capstone.entity.User;
import com.example.carmmunity_capstone.entity.UserProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserProviderRepository extends JpaRepository<UserProvider, Long> {
    // 특정 User 엔티티가 특정 Provider(카카오/네이버 등)와 이미 연동되어 있는지 확인합니다.
    Optional<UserProvider> findByUserAndProvider(User user, String provider);
    Optional<UserProvider> findByProviderAndProviderId(String provider, String providerId);
}
