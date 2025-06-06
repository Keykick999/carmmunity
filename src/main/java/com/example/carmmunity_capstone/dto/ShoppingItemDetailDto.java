package com.example.carmmunity_capstone.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShoppingItemDetailDto {
    private CarDetailDto car;
    private List<PartDetailDto> parts;
}