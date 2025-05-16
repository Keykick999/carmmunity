package com.example.carmmunity_capstone.repository;

import com.example.carmmunity_capstone.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

    Optional<EmailVerification> findTopByEmailAndPurposeOrderByCreatedAtDesc(String email, String purpose);
}
