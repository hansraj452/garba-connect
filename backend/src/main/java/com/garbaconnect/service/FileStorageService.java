package com.garbaconnect.service;

import com.garbaconnect.config.FileStorageConfig;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path uploadPath;

    public FileStorageService(FileStorageConfig config) throws IOException {

        this.uploadPath = Paths.get(config.getUploadDir()).toAbsolutePath().normalize();

        Files.createDirectories(uploadPath);
    }

    public String uploadProfileImage(MultipartFile file) throws IOException {

        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());

        String filename = UUID.randomUUID() + "." + extension;

        Path target = uploadPath.resolve(filename);

        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        return "/uploads/profile-images/" + filename;
    }

    public void deleteImage(String imageUrl) throws IOException {

        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }

        String filename = Paths.get(imageUrl).getFileName().toString();

        Files.deleteIfExists(uploadPath.resolve(filename));
    }
}