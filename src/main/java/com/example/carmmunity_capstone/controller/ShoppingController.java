package com.example.carmmunity_capstone.controller;

import com.example.carmmunity_capstone.dto.ComposeResponse;
import com.example.carmmunity_capstone.dto.ShoppingItemRequest;
import com.example.carmmunity_capstone.service.ShoppingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

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
    public ResponseEntity<List<String>> getList() {
        String userId = getAuthenticatedUserId();
        return ResponseEntity.ok(shoppingService.getCart(userId));
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
