package com.example.carmmunity_capstone.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ShoppingService {

    private final Map<String, Map<String, String>> userShoppingMap = new ConcurrentHashMap<>();

    public void addItem(String userId, String category, String imageName) {
        userShoppingMap.computeIfAbsent(userId, k -> new HashMap<>())
                .put(category, imageName); // 덮어쓰기
    }

    public Map<String, String> getShoppingList(String userId) {
        return userShoppingMap.getOrDefault(userId, new HashMap<>());
    }

    public void clearItem(String userId, String category) {
        Map<String, String> userMap = userShoppingMap.get(userId);
        if (userMap != null) userMap.remove(category);
    }

    public void clearAll(String userId) {
        userShoppingMap.remove(userId);
    }
}
