package com.devbuild.soutenanceservice.service;

import com.devbuild.soutenanceservice.config.FileStorageProperties;
import com.devbuild.soutenanceservice.exception.InvalidFileException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final FileStorageProperties properties;

    public String storeFile(MultipartFile file, Long doctorantId) {
        validateFile(file);

        String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path targetLocation = Paths.get(properties.getUploadDir(), doctorantId.toString(), filename);

        try {
            Files.createDirectories(targetLocation.getParent());
            Files.copy(file.getInputStream(), targetLocation);
            return targetLocation.toString();
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + filename, ex);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvalidFileException("Fichier vide");
        }
        String contentType = file.getContentType();
        if (!List.of("application/pdf", "image/jpeg", "image/png").contains(contentType)) {
            throw new InvalidFileException("Type de fichier non supporté");
        }
    }
}
