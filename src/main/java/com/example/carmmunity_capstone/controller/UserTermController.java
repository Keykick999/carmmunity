package com.example.carmmunity_capstone.controller;

import com.example.carmmunity_capstone.entity.UserTerm;
import com.example.carmmunity_capstone.service.UserTermService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user-terms")
public class UserTermController {

    private final UserTermService userTermService;

    public UserTermController(UserTermService userTermService) {
        this.userTermService = userTermService;
    }

    // ✅ 유저의 전체 약관 동의 내역 조회
    @GetMapping("/{userId}")
    public ResponseEntity<List<UserTerm>> getUserTerms(@PathVariable String userId) {
        return ResponseEntity.ok(userTermService.getUserTerms(userId));
    }

    // ✅ 유저의 특정 약관 동의 여부 조회
    @GetMapping("/{userId}/{termCode}")
    public ResponseEntity<UserTerm> getAgreement(@PathVariable String userId,
                                                 @PathVariable String termCode) {
        UserTerm result = userTermService.getAgreement(userId, termCode);
        return (result != null) ? ResponseEntity.ok(result) : ResponseEntity.notFound().build();
    }

    // ✅ 유저의 약관 동의 여부 수정
    @PutMapping("/{userId}/{termCode}")
    public ResponseEntity<String> updateAgreement(@PathVariable String userId,
                                                  @PathVariable String termCode,
                                                  @RequestParam boolean isAgreed) {
        userTermService.updateAgreement(userId, termCode, isAgreed);
        return ResponseEntity.ok("약관 동의 상태가 수정되었습니다.");
    }
}
