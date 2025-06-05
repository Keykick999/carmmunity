package com.example.carmmunity_capstone.controller;

import com.example.carmmunity_capstone.dto.ComposeResponse;
import com.example.carmmunity_capstone.dto.ShoppingItemRequest;
import com.example.carmmunity_capstone.dto.ShoppingItemResponse;
import com.example.carmmunity_capstone.service.ShoppingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/shopping")
@RequiredArgsConstructor
public class ShoppingController {

    private final ShoppingService shoppingService;


    @PostMapping("/add")
    public ResponseEntity<Void> addItem(@RequestBody ShoppingItemRequest request) {
        String userId = getAuthenticatedUserId();
        shoppingService.addImage(userId, request.getImageCode());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/remove")
    public ResponseEntity<Void> removeItem(@RequestBody ShoppingItemRequest request) {
        String userId = getAuthenticatedUserId();
        shoppingService.removeImage(userId, request.getImageCode());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/list")
    public ResponseEntity<List<ShoppingItemResponse>> getList() {
        String userId = getAuthenticatedUserId();
        List<String> codes = shoppingService.getCart(userId);

        List<ShoppingItemResponse> items = codes.stream()
                .map(code -> new ShoppingItemResponse(code, resolveLabel(code)))
                .toList();

        return ResponseEntity.ok(items);
    }

    private String resolveLabel(String code) {
        if (code.split("_").length >= 4) {
            String[] parts = code.split("_");
            return String.format("%s %s %s %s",
                    parts[0].toUpperCase(), parts[1].toUpperCase(), parts[2].toUpperCase(), parts[3].toUpperCase());
        }

        Map<String, String> brandMap = Map.of(
                "1", "Adro", "2", "Vörsteiner", "3", "AKRAPOVIČ"
        );
        Map<String, String> typeMap = Map.of(
                "wh", "휠", "ss", "사이드 스커트", "rd", "리어 디퓨저", "wi", "윙", "fl", "프론트립"
        );

        String brandCode = code.substring(0, 1);
        String partCode = code.substring(1);

        return brandMap.getOrDefault(brandCode, "Unknown") + " " +
                typeMap.getOrDefault(partCode, "Unknown");
    }


    @PostMapping("/compose")
    public ResponseEntity<ComposeResponse> compose() {
        String userId = getAuthenticatedUserId();
        String glbPath = shoppingService.composeGlbPath(userId);
        return ResponseEntity.ok(new ComposeResponse(glbPath));
    }

    private String getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("인증되지 않은 사용자입니다.");
        }
        return authentication.getName();
    }
}
