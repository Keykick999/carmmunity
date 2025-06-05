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
    //C:/uploads/2d/{manufacturer}/{model}/{codeName}/{imageName}
    //car-list에서 넣기 버튼 누르면 차량이 담긴다. -> 우측 하단에 파츠 선택하러 가기 ->  parts-list에서 넣기 버튼 누르면 파츠 담기게


    // 차량(제조사, 모델명, 색상(이미지명)), 부품
    //wheel : wh , side skirt : ss, rear diffuser : rd, wing : wi, wrapping : fl : frontlib
    //Adro : 1, Vörsteiner : 2, AKRAPOVIČ : 3

    //예시
    //C:/uploads/2d/bmw/m4/g82/bmw_m4_g82_white
    //C:/uploads/2d/bmw/m4/g82/1wh
    //C:/uploads/2d/bmw/m4/g82/2ss
    //C:/uploads/2d/bmw/m4/g82/2wi
    //C:/uploads/2d/bmw/m4/g82/3rd

    //C:/uploads/3d/ bmw, m4, g82, white, 1wh, 2ss, 2wi, 3rd

    //최종 조합 형태 : split통해서 분해 bmw, m4, g82, white, 1wh, 2ss, 2wi, 3rd -> 이후에 조합해서 맞는 이미지

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