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

    // 차량 및 부품 2D 이미지 (모두 같은 구조에서 조회)
    @GetMapping("/2d")
    public ResponseEntity<List<String>> get2DImages(
            @RequestParam String manufacturer,
            @RequestParam String model,
            @RequestParam String codeName
    ) {
        List<String> imagePaths = tuningService.get2DImages(manufacturer, model, codeName);
        return ResponseEntity.ok(imagePaths);
    }

    // 특정 파츠 코드 (1wh, 2ss 등)를 포함하는 이미지만 필터링
    @GetMapping("/2d/filter")
    public ResponseEntity<List<String>> getFilteredParts(
            @RequestParam String manufacturer,
            @RequestParam String model,
            @RequestParam String codeName,
            @RequestParam String partSuffix // 예: wh, rd, wi 등
    ) {
        List<String> imagePaths = tuningService.getFilteredParts(manufacturer, model, codeName, partSuffix);
        return ResponseEntity.ok(imagePaths);
    }
}
