package com.example.carmmunity_capstone.controller;

import com.example.carmmunity_capstone.service.TuningService;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/tuning")
public class TuningController {

    private final TuningService tuningService;

    public TuningController(TuningService tuningService) {
        this.tuningService = tuningService;
    }

    @GetMapping("/cars")
    public List<String> getCarImages(@RequestParam String manufacturer,
                                     @RequestParam String model,
                                     @RequestParam String codeName) {
        return tuningService.findCarImages(manufacturer, model, codeName);
    }

    @GetMapping("/parts")
    public List<String> getPartImages(@RequestParam String manufacturer,
                                      @RequestParam String model,
                                      @RequestParam String codeName,
                                      @RequestParam String partTypePrefix) {
        return tuningService.findPartImages(manufacturer, model, codeName, partTypePrefix);
    }
}

