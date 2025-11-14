package com.example.carmmunity_capstone.exception;// com.example.carmmunity_capstone.exception/EmailDuplicateForLinkingException.java (수정)

// 💡 [수정] RuntimeException 대신 CustomException을 상속합니다.
public class EmailDuplicateForLinkingException extends CustomException {

    // 💡 [수정] 생성자를 통해 고정된 ErrorCode를 전달합니다.
    public EmailDuplicateForLinkingException() {
        super(ErrorCode.EMAIL_DUPLICATE_FOR_LINKING);
    }
}