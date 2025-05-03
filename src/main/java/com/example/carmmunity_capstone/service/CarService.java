package com.example.carmmunity_capstone.service;

import com.example.carmmunity_capstone.domain.User;
import com.example.carmmunity_capstone.domain.UserCar;
import com.example.carmmunity_capstone.domain.UserCarId;
import com.example.carmmunity_capstone.dto.UserCarUploadRequestDto;
import com.example.carmmunity_capstone.exception.TooManyUserCarsException;
import com.example.carmmunity_capstone.repository.UserCarRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CarService {

    private final UserCarRepository userCarRepository;
    private final UserService userService;

    public CarService(UserCarRepository userCarRepository, UserService userService) {
        this.userCarRepository = userCarRepository;
        this.userService = userService;
    }


    // 튜닝 후 차량 저장
    public void registerTunedCar(UserCarUploadRequestDto userCarUploadRequestDto, String authorizationHeader) {
        // 인증된 회원인지 확인
        User user = userService.validateUser(authorizationHeader);


        // UserCar에서 회원 id값으로 조회해서 몇 개 있는지 확인   TODO 삭제일시나 생성일시 추가해야
        for (int slot = 1; slot <= 3; slot++) {
            UserCarId tryId = new UserCarId(user.getId(), slot);
            if (!userCarRepository.existsByIdAndDeletedAtIsNull(tryId)) {    // 비어있는 슬롯이면 저장
                UserCar userCar = UserCar.builder()
                        .id(tryId)
                        .user(user)
                        .imagePath(userCarUploadRequestDto.getImagePath())
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .deletedAt(null)
                        .build();
                userCarRepository.save(userCar);
                return;
            }
        }

        // 비어있는 슬롯 없으면 에러
        throw new TooManyUserCarsException();
    }
}




