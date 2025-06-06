package com.example.carmmunity_capstone.service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TuningService {
    @Value("${tuning.image.base-path-2d}")
    private String base2dPath;

    public List<String> findCarImages(String manufacturer, String model, String codeName) {
        String path = String.join(File.separator,
                base2dPath, manufacturer, model, codeName, "cars");
        System.out.println("path = " + path);
        return findImagesInDirectory(path);
    }

    public List<String> findPartImages(String manufacturer, String model, String codeName, String partTypePrefix) {
        String path = String.join(File.separator,
                base2dPath, manufacturer, model, codeName, "parts");
        return findImagesInDirectory(path).stream()
                .filter(name -> name.startsWith(partTypePrefix))
                .collect(Collectors.toList());
    }

    private List<String> findImagesInDirectory(String fullPath) {
        File dir = new File(fullPath);
        if (!dir.exists() || !dir.isDirectory()) return Collections.emptyList();

        File[] files = dir.listFiles((d, name) -> name.endsWith(".png") || name.endsWith(".jpg"));
        if (files == null) return Collections.emptyList();

        return Arrays.stream(files).map(File::getName).collect(Collectors.toList());
    }
}