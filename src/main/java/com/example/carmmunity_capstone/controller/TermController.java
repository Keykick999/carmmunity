package com.example.carmmunity_capstone.controller;

import com.example.carmmunity_capstone.entity.Term;
import com.example.carmmunity_capstone.repository.TermRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/terms")
public class TermController {

    private final TermRepository termRepository;

    public TermController(TermRepository termRepository) {
        this.termRepository = termRepository;
    }

    // 전체 약관 목록 (정렬 포함)
    @GetMapping
    public ResponseEntity<List<Term>> getAllTerms() {
        List<Term> terms = termRepository.findAllByOrderByDisplayOrderAsc();
        return ResponseEntity.ok(terms);
    }

    // 필수 약관만 조회
    @GetMapping("/required")
    public ResponseEntity<List<Term>> getRequiredTerms() {
        List<Term> terms = termRepository.findByIsRequiredTrueOrderByDisplayOrderAsc();
        return ResponseEntity.ok(terms);
    }

    // 선택 약관만 조회
    @GetMapping("/optional")
    public ResponseEntity<List<Term>> getOptionalTerms() {
        List<Term> terms = termRepository.findByIsRequiredFalseOrderByDisplayOrderAsc();
        return ResponseEntity.ok(terms);
    }

    // 특정 약관 상세 조회 (운영자 수정용)
    @GetMapping("/{termCode}")
    public ResponseEntity<Term> getTermByCode(@PathVariable String termCode) {
        return termRepository.findById(termCode)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
