package com.example.carmmunity_capstone.service;

import com.example.carmmunity_capstone.dto.UserTermAgreementRequest;
import com.example.carmmunity_capstone.entity.UserTerm;
import com.example.carmmunity_capstone.entity.UserTermId;
import com.example.carmmunity_capstone.repository.UserTermRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserTermService {

    private final UserTermRepository userTermRepository;

    public UserTermService(UserTermRepository userTermRepository) {
        this.userTermRepository = userTermRepository;
    }

    /**
     * 회원가입 시 약관 동의 정보 저장
     */
    @Transactional
    public void saveUserAgreements(String userId, List<UserTermAgreementRequest> agreements) {
        for (UserTermAgreementRequest dto : agreements) {
            UserTerm userTerm = UserTerm.builder()
                    .userId(userId)
                    .termCode(dto.getTermCode())
                    .isAgreed(dto.isAgreed())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            userTermRepository.save(userTerm);
        }
    }

    /**
     * 특정 사용자 약관 전체 조회
     */
    public List<UserTerm> getUserTerms(String userId) {
        return userTermRepository.findByUserId(userId);
    }

    /**
     * 특정 사용자 약관 동의 여부 단일 조회
     */
    public UserTerm getAgreement(String userId, String termCode) {
        return userTermRepository.findByUserIdAndTermCode(userId, termCode)
                .orElse(null);
    }

    /**
     * 특정 사용자 약관 동의 상태 수정
     */
    @Transactional
    public void updateAgreement(String userId, String termCode, boolean isAgreed) {
        UserTermId id = new UserTermId(userId, termCode);
        UserTerm userTerm = userTermRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 약관 동의 정보를 찾을 수 없습니다."));
        userTerm.setIsAgreed(isAgreed);
        userTerm.setUpdatedAt(LocalDateTime.now());
        userTermRepository.save(userTerm);
    }
}
