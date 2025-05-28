package com.example.carmmunity_capstone.controller;
import com.example.carmmunity_capstone.dto.ShoppingItemRequest;
import com.example.carmmunity_capstone.service.ShoppingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/shopping")
public class ShoppingController {

    private final ShoppingService shoppingService;

    public ShoppingController(ShoppingService shoppingService) {
        this.shoppingService = shoppingService;
    }

    @PostMapping("/add")
    public ResponseEntity<Void> addItem(@RequestBody ShoppingItemRequest dto) {
        String userId = getAuthenticatedUserId();
        shoppingService.addItem(userId, dto.getCategory(), dto.getImageName());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/list")
    public ResponseEntity<Map<String, String>> getList() {
        String userId = getAuthenticatedUserId();
        return ResponseEntity.ok(shoppingService.getShoppingList(userId));
    }

    @PostMapping("/remove")
    public ResponseEntity<Void> removeItem(@RequestBody ShoppingItemRequest dto) {
        String userId = getAuthenticatedUserId();
        shoppingService.clearItem(userId, dto.getCategory());
        return ResponseEntity.ok().build();
    }

    private String getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("로그인된 사용자가 없습니다.");
        }

        return authentication.getName();
    }
}

