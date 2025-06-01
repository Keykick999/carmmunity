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
    //C:/uploads/2d/bmw/m4-g82/car/m4실제 컬러 코드명.png

    //C:/uploads/2d/bmw/m4-g82/car_차량 색상  C:/uploads/2d/bmw/m4-g82/parts_kangwon_wheel_휠 코드 네임_휠 색상
    //아니면 차라리 우리가 새로 코드네임을 만들어 줄까?

    //car_차량 색상  + parts_브랜드명_어떤 파츠인지 + parts~~ = 3d

    // (차량: 제조사, 모델명-코드명, 이미지명(?), 부품: 제조사, 부품종류, )
    //C:/uploads/3d/b/d/parts/wheel.png

    //car-list에서 넣기 버튼 누르면 차량이 담긴다. -> 우측 하단에 파츠 선택하러 가기 ->  parts-list에서 넣기 버튼 누르면 파츠 담기게


    // 차량(제조사, 모델명, 색상(이미지명)), 부품
    //wheel : wh , side skirt : ss, rear diffuser : rd, wing : wi, wrapping : wr , front bumper : fb....
    //Adro : 1, Vörsteiner : 2, AKRAPOVIČ : 3  ...

    //C:/uploads/2d/bmw/m4/g82/bmw_m4_g82_white
    //C:/uploads/2d/bmw/m4/g82/1wh
    //C:/uploads/2d/bmw/m4/g82/2ss
    //C:/uploads/2d/bmw/m4/g82/2wi
    //C:/uploads/2d/bmw/m4/g82/3rd

    //C:/uploads/3d/ bmw, m4, g82, white, 1wh, 2ss, 2wi, 3rd

    //최종 조합 형태 : split통해서 분해 bmw, m4, g82, white, 1wh, 2ss, 2wi, 3rd -> 이후에 조합해서 맞는 이미지


    //튜닝 파츠에 대해 얘네는 카본이냐 아니냐도 있어
    //wheel : wh , side skirt : ss, rear diffuser : rd, wing : wi, wrapping : wr , front bumper : fb....
    //튜닝 브랜드
    //Adro, Vörsteiner, AKRAPOVIČ





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



    public List<String> getPartImages(String carManufacturer, String carModelCode, String partBrand, String partType) {
        Path targetDir = Paths.get(base2DPath, carManufacturer, carModelCode, "parts", partBrand, partType);

        if (!Files.exists(targetDir)) return Collections.emptyList();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(targetDir)) {
            return StreamSupport.stream(stream.spliterator(), false)
                    .filter(Files::isRegularFile)
                    .map(path -> String.format(
                            "/uploads/2d/%s/%s/parts/%s/%s/%s",
                            carManufacturer, carModelCode, partBrand, partType, path.getFileName()
                    ))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("2D 파츠 이미지 조회 중 오류 발생", e);
        }
    }
}
