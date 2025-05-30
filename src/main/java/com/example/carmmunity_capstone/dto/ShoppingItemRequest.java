package com.example.carmmunity_capstone.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingItemRequest {
    private String imageCode; // 차량: bmw_m4_g82_white, 파츠: 1wh, 2ss 등
}
