package com.example.carmmunity_capstone.controller;
import com.example.carmmunity_capstone.service.TuningService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequestMapping("/tuning")
@RequiredArgsConstructor
public class TuningController {

    private final TuningService tuningService;


    @GetMapping("/2d")
    public ResponseEntity<List<String>> get2DImages(
            @RequestParam String manufacturer,
            @RequestParam String modelCode,
            @RequestParam String category
    ) {
        List<String> imagePaths = tuningService.get2DImages(manufacturer, modelCode, category);
        return ResponseEntity.ok(imagePaths);
    }


    // 파츠 검색
    @GetMapping("/2d/parts")
    public ResponseEntity<List<String>> getPartImages(
            @RequestParam String carManufacturer,
            @RequestParam String carModelCode,
            @RequestParam String partBrand,
            @RequestParam String partType
    ) {
        List<String> imagePaths = tuningService.getPartImages(carManufacturer, carModelCode, partBrand, partType);
        return ResponseEntity.ok(imagePaths);
    }
}