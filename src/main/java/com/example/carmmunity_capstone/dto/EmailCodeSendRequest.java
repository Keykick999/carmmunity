package com.example.carmmunity_capstone.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class EmailCodeSendRequest {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String purpose;
}
