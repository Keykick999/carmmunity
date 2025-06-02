package com.example.carmmunity_capstone.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserTermAgreementRequest {
    private String termCode;

    @JsonProperty("isAgreed") // ✅ Jackson 매핑 보장
    private boolean isAgreed;
}
