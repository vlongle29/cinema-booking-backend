package com.example.CineBook.service.impl;

import com.example.CineBook.common.exception.BusinessException;
import com.example.CineBook.common.exception.MessageCode;
import com.example.CineBook.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${file.upload-dir:uploads/posters}")
    private String uploadDir;

    @Override
    public String storeFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException(MessageCode.INVALID_INPUT);
        }

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";

        String filename = UUID.randomUUID() + extension;

        try {
            // Bước 1: Tạo đối tượng path từ đường dẫn thư mực
            Path uploadPath = Paths.get(uploadDir);

            // Bước 2: Kiểm tra thư mục có tồn tại chưa
            if (!Files.exists(uploadPath)) {
                // Nếu chưa có → tạo thư mục (bao gồm cả thư mục cha nếu cần)
                Files.createDirectories(uploadPath);
            }

            // Bước 3: Tạo đường dẫn đầy đủ đến file
            Path filePath = uploadPath.resolve(filename);

            // Bước 4: Copy nội dung file từ request vào đường dẫn đã tạo
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/posters/" + filename;
        } catch (IOException e) {
            log.error("Failed to store file", e);
            throw new BusinessException(MessageCode.INTERNAL_SERVER_ERROR);
        }
    }
}
