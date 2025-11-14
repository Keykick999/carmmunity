package com.example.carmmunity_capstone.exception;

import org.springframework.security.core.AuthenticationException;
import com.example.carmmunity_capstone.dto.OAuth2FailureData;

public class AccountNeedsLinkingException extends AuthenticationException {

    private final OAuth2FailureData data;

    public AccountNeedsLinkingException(OAuth2FailureData data, String msg) {
        super(msg);
        this.data = data;
    }

    public OAuth2FailureData getData() {
        return data;
    }
}