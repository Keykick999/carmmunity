package com.example.carmmunity_capstone.service;

import com.example.carmmunity_capstone.entity.User;
import com.example.carmmunity_capstone.entity.UserCar;
import com.example.carmmunity_capstone.entity.UserCarId;
import com.example.carmmunity_capstone.dto.UserCarUploadRequestDto;
import com.example.carmmunity_capstone.exception.CarNotFoundException;
import com.example.carmmunity_capstone.exception.TooManyUserCarsException;
import com.example.carmmunity_capstone.repository.UserCarRepository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    public void registerTunedCar(UserCarUploadRequestDto userCarUploadRequestDto) {
        User user = userService.getAuthenticatedUser();


        // UserCar에서 회원 id값으로 조회해서 몇 개 있는지 확인   TODO 삭제일시나 생성일시 추가해야
        for (int slot = 1; slot <= 3; slot++) {
            UserCarId tryId = new UserCarId(user.getUserId(), slot);
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


    // 튜닝된 차량 조회
    public Resource getTunedCar(Integer carId) throws MalformedURLException {
        User user = userService.getAuthenticatedUser();

        UserCarId userCarId = new UserCarId(user.getUserId(), carId);

        UserCar userCar = userCarRepository.findByIdAndDeletedAtIsNull(userCarId)
                .orElseThrow(() -> new CarNotFoundException("해당 차량을 찾을 수 없습니다."));

        Path imagePath = Paths.get(userCar.getImagePath());
        return new UrlResource(imagePath.toUri());
    }
}




