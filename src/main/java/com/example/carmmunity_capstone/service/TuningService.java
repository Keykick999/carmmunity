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


    @Value("${tuning.image.base-path-2d}")
    private String base2DPath;

    // 차량 및 파츠 2D 이미지 조회 (동일한 경로 구조 사용)
    public List<String> get2DImages(String manufacturer, String model, String codeName) {
        Path targetDir = Paths.get(base2DPath, manufacturer, model, codeName);

        if (!Files.exists(targetDir)) return Collections.emptyList();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(targetDir)) {
            return StreamSupport.stream(stream.spliterator(), false)
                    .filter(Files::isRegularFile)
                    .map(path -> String.format("/uploads/2d/%s/%s/%s/%s",
                            manufacturer, model, codeName, path.getFileName()))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("2D 이미지 조회 중 오류 발생 - 경로: " + targetDir, e);
        }
    }

    // 특정 파츠 코드만 필터링하고 싶을 때 (예: "wh" 또는 "1wh")
    public List<String> getFilteredParts(String manufacturer, String model, String codeName, String partSuffix) {
        Path targetDir = Paths.get(base2DPath, manufacturer, model, codeName);

        if (!Files.exists(targetDir)) return Collections.emptyList();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(targetDir)) {
            return StreamSupport.stream(stream.spliterator(), false)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(partSuffix + ".png")) // 예: 1wh.png
                    .map(path -> String.format("/uploads/2d/%s/%s/%s/%s",
                            manufacturer, model, codeName, path.getFileName()))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("파츠 필터링 중 오류 발생 - 경로: " + targetDir, e);
        }
    }
}