package com.example.carmmunity_capstone.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCarUploadRequestDto {

    //원하는 차량의 슬롯을 설정해서 저장하기 위한 추가 변수
    private Integer carId;       // 사용자가 저장하려는 슬롯 (1~3)

    private String imagePath;    // 저장할 이미지 경로
}
