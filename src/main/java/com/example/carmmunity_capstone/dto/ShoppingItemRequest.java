package com.example.carmmunity_capstone.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingItemRequest {
    private String category;     // car, wing, bonnet 등
    private String imageName;    // 선택된 파일명
}