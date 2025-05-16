package com.example.carmmunity_capstone.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FindIdRequest {

    @NotBlank
    @Email
    private String email;
}
