package com.example.carmmunity_capstone.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {
    private String email;
    private String verificationCode;
    private String newPassword;
}
