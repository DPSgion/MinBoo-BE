package com.phobo.post.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileUploadService {
    // Thư mục gốc của resources/static
    public String uploadImage(MultipartFile file, String subDir) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID() + extension;

            // Đường dẫn lưu file: src/main/resources/static/uploads/subDir
            Path uploadDir = Paths.get("src/main/resources/static/upload", subDir);
            Files.createDirectories(uploadDir);          // tạo thư mục nếu chưa có
            Path filePath = uploadDir.resolve(fileName);
            Files.write(filePath, file.getBytes());

            // URL trả về để client truy cập
            return "/upload/" + subDir + "/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Upload file thất bại: " + e.getMessage(), e);
        }
    }
}
