package com.example.carmmunity_capstone.controller;
import com.example.carmmunity_capstone.dto.UserCarUploadRequestDto;
import com.example.carmmunity_capstone.service.CarService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cars")
public class CarController {

    private final CarService carService;

    public CarController(CarService carService) {
        this.carService = carService;
    }


    // 튜닝 차량 저장
    @PostMapping
    public ResponseEntity<String> registerTunedCar(@RequestBody UserCarUploadRequestDto userCarUploadRequestDto, @RequestHeader(value = "Authorization", required = true) String authorizationHeader) {
        carService.registerTunedCar(userCarUploadRequestDto, authorizationHeader);
        return ResponseEntity.status(HttpStatus.CREATED).body("차량 튜닝이 성공적으로 되었습니다");
    }
}
