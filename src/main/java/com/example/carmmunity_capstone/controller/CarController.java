package com.example.carmmunity_capstone.controller;

import com.example.carmmunity_capstone.dto.UserCarUploadRequestDto;
import com.example.carmmunity_capstone.dto.UserCarResponseDto;
import com.example.carmmunity_capstone.service.CarService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.util.List;

@RestController
@RequestMapping("/cars")
public class CarController {

    private final CarService carService;

    public CarController(CarService carService) {
        this.carService = carService;
    }

    // 튜닝 차량 저장
    @PostMapping
    public ResponseEntity<String> registerTunedCar(@RequestBody UserCarUploadRequestDto dto) {
        carService.registerTunedCar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body("차량 튜닝이 성공적으로 저장되었습니다.");
    }

    // 튜닝 차량 단일 조회
    @GetMapping
    public ResponseEntity<Resource> getTunedCar(@RequestParam("carId") Integer carId) throws MalformedURLException {
        Resource glbFile = carService.getTunedCar(carId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + glbFile.getFilename() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)  // 🔥 중요
                .body(glbFile);
    }


    // 모든 슬롯 리스트 조회
    @GetMapping("/list")
    public ResponseEntity<List<UserCarResponseDto>> getUserCarList() {
        return ResponseEntity.ok(carService.getUserCarList());
    }
}
