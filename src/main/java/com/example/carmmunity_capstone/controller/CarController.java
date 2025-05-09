package com.example.carmmunity_capstone.controller;
import com.example.carmmunity_capstone.dto.UserCarUploadRequestDto;
import com.example.carmmunity_capstone.service.CarService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;

@RestController
@RequestMapping("/cars")
public class CarController {

    private final CarService carService;

    public CarController(CarService carService) {
        this.carService = carService;
    }


    // 튜닝 차량 저장
    @PostMapping
    public ResponseEntity<String> registerTunedCar(@RequestBody UserCarUploadRequestDto userCarUploadRequestDto) {
        carService.registerTunedCar(userCarUploadRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body("차량 튜닝이 성공적으로 되었습니다");
    }


    // 튜닝 차량 조회
    @GetMapping
    public ResponseEntity<Resource> getTunedCar(@RequestParam("carId") Integer carId) throws MalformedURLException {
        return ResponseEntity.ok(carService.getTunedCar(carId));
    }
}
