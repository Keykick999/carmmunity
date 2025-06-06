package com.example.carmmunity_capstone.service;

import com.example.carmmunity_capstone.dto.CarDetailDto;
import com.example.carmmunity_capstone.dto.PartDetailDto;
import com.example.carmmunity_capstone.dto.ShoppingItemDetailDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.nio.file.*;

@Slf4j
@Service
public class ShoppingService {
    private static final Map<String, String> brandNameMap = Map.of(
            "1", "Adro",
            "2", "Vörsteiner",
            "3", "AKRAPOVIČ"
    );

    private static final Map<String, String> partTypeNameMap = Map.of(
            "wh", "휠",
            "ss", "사이드 스커트",
            "rd", "리어 디퓨저",
            "wi", "윙",
            "fl", "프론트립"
    );

    // userId → 이미지 코드 리스트 (차량 + 파츠 포함)
    // key : user1 , value : 이미지경로
    private final Map<String, List<String>> shoppingCart = new ConcurrentHashMap<>();


    // 이미지 추가 (중복 파츠 유형은 제거 후 추가)
    public void addImage(String userId, String imageCode) {
        List<String> cart = shoppingCart.computeIfAbsent(userId, k -> new ArrayList<>());

        // 기존 동일 파츠 유형 제거 (car or partType)
        String newType = extractPartType(imageCode);
        cart.removeIf(code -> extractPartType(code).equals(newType));

        cart.add(imageCode);
    }

    // 항목 제거
    public void removeImage(String userId, String imageCode) {
        List<String> cart = shoppingCart.get(userId);
        if (cart != null) {
            cart.removeIf(code -> code.equals(imageCode));
        }
    }

    // 전체 목록 반환
    public List<String> getCart(String userId) {
        return shoppingCart.getOrDefault(userId, new ArrayList<>());
    }

    // 장바구니 초기화
    public void clearCart(String userId) {
        shoppingCart.remove(userId);
    }

    // 최종 GLB key 조합
    public String composeGlbKey(List<String> imageCodes) {
        List<String> sorted = new ArrayList<>(imageCodes);
        Collections.sort(sorted); // 정렬 기준 일관성 유지
        return String.join("_", sorted);
    }

    // 최종 GLB 경로 반환
    public String composeGlbPath(String userId) {
        List<String> imageCodes = shoppingCart.get(userId);
        if (imageCodes == null || imageCodes.isEmpty()) {
            System.out.println("선택된 이미지 없음");
            throw new IllegalStateException("선택된 이미지가 없습니다.");
        }

        String key = composeGlbKey(imageCodes);
        Path path = Paths.get("C:/uploads/3d", key + ".glb");

        if (!Files.exists(path)) {
            System.out.println("해당 조합 3D 모델 존재 x");
            throw new IllegalStateException("해당 조합의 3D 모델이 존재하지 않습니다: " + path);
        }

        return "/uploads/3d/" + key + ".glb";
    }

    // 이미지 코드에서 파츠 유형 추출 (예: "1wh" → "wh", 차량은 "car")
    private String extractPartType(String code) {
        if (code != null && code.split("_").length >= 4) {
            return "car"; // 차량 이미지 (예: bmw_m4_g82_white)
        }
        return code.replaceAll("[^a-z]", ""); // 예: 2ss → ss
    }

    public ShoppingItemDetailDto getShoppingDetails(String userId) {
        List<String> codes = shoppingCart.getOrDefault(userId, Collections.emptyList());
        CarDetailDto car = null;
        List<PartDetailDto> parts = new ArrayList<>();

        for (String code : codes) {
            String type = extractPartType(code);

            if ("car".equals(type)) {
                String[] tokens = code.split("_");
                if (tokens.length >= 4) {
                    car = new CarDetailDto(tokens[0], tokens[1], tokens[2], tokens[3]);
                }
            } else {
                String brandCode = code.substring(0, 1);
                String partCode = code.substring(1);
                String brand = brandNameMap.getOrDefault(brandCode, "Unknown");
                String partType = partTypeNameMap.getOrDefault(partCode, "Unknown");
                parts.add(new PartDetailDto(brand, partType));
            }
        }

        return new ShoppingItemDetailDto(car, parts);
    }
}
