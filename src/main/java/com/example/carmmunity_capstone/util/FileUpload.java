package com.example.carmmunity_capstone.util;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileUpload {

    private final String uploadDir = "C:/uploads"; // 예: C:/uploads 또는 /home/ubuntu/uploads

    public String store(MultipartFile file) {
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path path = Paths.get(uploadDir, filename);

        try {
            Files.createDirectories(path.getParent());
            file.transferTo(path.toFile());
            return "/uploads/" + filename; // 이 경로로 웹에서 접근 가능하게 static 설정 필요
        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 실패", e);
        }
    }
}
