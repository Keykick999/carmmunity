package com.example.carmmunity_capstone.config;

import com.example.carmmunity_capstone.entity.Term;
import com.example.carmmunity_capstone.repository.TermRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


@Component
public class TermDataInitializer implements CommandLineRunner {

    private final TermRepository termRepository;

    public TermDataInitializer(TermRepository termRepository) {
        this.termRepository = termRepository;
    }

    @Override
    public void run(String... args) {
        insertIfNotExists("T001", "서비스 이용약관", true, 1,
                "이 약관은 본 서비스의 이용과 관련하여 회원과 회사 간의 권리, 의무 및 책임사항을 규정합니다. ...");

        insertIfNotExists("T002", "개인정보 수집 및 이용 동의", true, 2,
                "회사는 서비스 제공을 위해 최소한의 개인정보(이름, 이메일, 전화번호 등)를 수집 및 이용합니다. ...");

        insertIfNotExists("T003", "마케팅 정보 수신 동의", false, 3,
                "신상품 소식, 이벤트 안내 등 마케팅 목적의 이메일/SMS를 수신하는 것에 동의합니다. ...");
    }

    private void insertIfNotExists(String code, String title, boolean required, int order, String content) {
        if (!termRepository.existsById(code)) {
            Term term = Term.builder()
                    .termCode(code)
                    .termTitle(title)
                    .termContent(content)
                    .isRequired(required)
                    .displayOrder(order)
                    .build();
            termRepository.save(term);
        }
    }
}
