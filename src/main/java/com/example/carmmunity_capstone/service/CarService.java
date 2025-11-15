package com.example.carmmunity_capstone.service;

import com.example.carmmunity_capstone.entity.User;
import com.example.carmmunity_capstone.entity.UserCar;
import com.example.carmmunity_capstone.entity.UserCarId;
import com.example.carmmunity_capstone.dto.UserCarUploadRequestDto;
import com.example.carmmunity_capstone.dto.UserCarResponseDto;
import com.example.carmmunity_capstone.exception.CarNotFoundException;
import com.example.carmmunity_capstone.repository.UserCarRepository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CarService {

    private final UserCarRepository userCarRepository;
    private final UserService userService;

    public CarService(UserCarRepository userCarRepository, UserService userService) {
        this.userCarRepository = userCarRepository;
        this.userService = userService;
    }

    // 차량 저장 (슬롯 선택 또는 첫 저장은 slot 1 강제)
    public void registerTunedCar(UserCarUploadRequestDto dto) {
        User user = userService.getAuthenticatedUser();
        int currentCount = userCarRepository.countUserCars(user.getUserUuid());

        Integer carId = dto.getCarId();

        // 첫 저장인 경우 slot 1 강제 지정
        if (currentCount == 0) {
            carId = 1;
        }

        // 잘못된 슬롯 번호는 무시
        if (carId == null || carId < 1 || carId > 3) {
            return;
        }

        UserCarId id = new UserCarId(user.getUserUuid(), carId);

        UserCar userCar = UserCar.builder()
                .id(id)
                .user(user)
                .imagePath(dto.getImagePath())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .deletedAt(null)
                .build();

        userCarRepository.save(userCar); // 기존 차량이 있으면 덮어씀
    }

    // 단일 차량 조회
    public Resource getTunedCar(Integer carId) throws MalformedURLException {
        User user = userService.getAuthenticatedUser();
        UserCarId userCarId = new UserCarId(user.getUserUuid(), carId);

        UserCar userCar = userCarRepository.findByIdAndDeletedAtIsNull(userCarId)
                .orElseThrow(() -> new CarNotFoundException("해당 차량을 찾을 수 없습니다."));

        Path imagePath = Paths.get(userCar.getImagePath());
        return new UrlResource(imagePath.toUri());
    }

    // 3개 슬롯 전체 목록 조회
    public List<UserCarResponseDto> getUserCarList() {
        User user = userService.getAuthenticatedUser();

        return userCarRepository.findAllByUser_UserUuidAndDeletedAtIsNull(user.getUserUuid())
                .stream()
                .map(car -> new UserCarResponseDto(car.getId().getCarId(), car.getImagePath()))
                .toList();
    }
}
