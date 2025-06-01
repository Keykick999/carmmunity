package com.example.carmmunity_capstone.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserTermAgreementRequest {
    private String termCode;
    private boolean isAgreed;
}
