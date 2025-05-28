package com.example.carmmunity_capstone.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class TuningService {
    //C:/uploads/2d/{manufacturer}/{model}-{codeName}/{car or parts}/{imageName}
    //C:/uploads/2d/bmw/m4-g82/car/car_bmw_m4_black.png
    @Value("${tuning.image.base-path-2d}")
    private String base2DPath;

    public List<String> get2DImages(String manufacturer, String modelCode, String category) {
        Path targetDir = Paths.get(base2DPath, manufacturer, modelCode, category);

        if (!Files.exists(targetDir)) return Collections.emptyList();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(targetDir)) {
            return StreamSupport.stream(stream.spliterator(), false)
                    .filter(Files::isRegularFile)
                    .map(path -> String.format("/uploads/2d/%s/%s/%s/%s",
                            manufacturer, modelCode, category, path.getFileName()))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("2D 이미지 조회 중 오류 발생", e);
        }
    }
}
