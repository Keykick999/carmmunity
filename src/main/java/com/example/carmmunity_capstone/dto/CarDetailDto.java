package com.example.carmmunity_capstone.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CarDetailDto {
    private String manufacturer;
    private String model;
    private String codeName;
    private String carName;
}
