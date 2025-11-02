package com.devbuild.inscriptionservice.service;

import com.devbuild.inscriptionservice.config.FileStorageProperties;
import com.devbuild.inscriptionservice.exception.InvalidFileException;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;


@Service
@Slf4j
public class FileStorageService {
    private final Path fileStorageLocation;
    private final FileStorageProperties properties;
    private final Tika tika;

    public FileStorageService(FileStorageProperties properties) {
        this.properties = properties;
        this.tika = new Tika();
        this.fileStorageLocation = Paths.get(properties.getStorage().getPath())
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
            log.info("File storage directory created: {}", this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create upload directory!", ex);
        }
    }

    public String storeFile(MultipartFile file, Long dossierId) {
        validateFile(file);

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = getExtension(originalFilename);
        String newFilename = String.format("%d_%s.%s",
                dossierId,
                UUID.randomUUID().toString(),
                extension);

        try {
            if (originalFilename.contains("..")) {
                throw new InvalidFileException("Filename contains invalid path sequence: " + originalFilename);
            }

            Path targetLocation = this.fileStorageLocation.resolve(newFilename);

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }

            log.info("File stored successfully: {}", newFilename);
            return newFilename;

        } catch (IOException ex) {
            log.error("Failed to store file: {}", originalFilename, ex);
            throw new RuntimeException("Could not store file " + originalFilename, ex);
        }
    }

    public void deleteFile(String filename) {
        try {
            Path filePath = this.fileStorageLocation.resolve(filename).normalize();
            Files.deleteIfExists(filePath);
            log.info("File deleted: {}", filename);
        } catch (IOException ex) {
            log.error("Failed to delete file: {}", filename, ex);
        }
    }

    public Path getFilePath(String filename) {
        return this.fileStorageLocation.resolve(filename).normalize();
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvalidFileException("Le fichier est vide");
        }

        if (file.getSize() > properties.getFile().getMaxSize()) {
            throw new InvalidFileException(
                    String.format("Le fichier dépasse la taille maximale autorisée de %d MB",
                            properties.getFile().getMaxSize() / (1024 * 1024))
            );
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new InvalidFileException("Le nom du fichier est invalide");
        }

        String extension = getExtension(originalFilename);
        if (!properties.getFile().getAllowedExtensions().contains(extension.toLowerCase())) {
            throw new InvalidFileException(
                    String.format("Extension de fichier non autorisée: %s. Extensions autorisées: %s",
                            extension, String.join(", ", properties.getFile().getAllowedExtensions()))
            );
        }

        // Validate MIME type with Tika
        try {
            String detectedMimeType = tika.detect(file.getInputStream());
            if (!isAllowedMimeType(detectedMimeType)) {
                throw new InvalidFileException(
                        "Le type MIME du fichier n'est pas autorisé: " + detectedMimeType
                );
            }
        } catch (IOException e) {
            throw new InvalidFileException("Impossible de détecter le type MIME du fichier");
        }
    }

    private String getExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot + 1) : "";
    }

    private boolean isAllowedMimeType(String mimeType) {
        return mimeType.equals("application/pdf") ||
                mimeType.startsWith("image/jpeg") ||
                mimeType.startsWith("image/jpg") ||
                mimeType.startsWith("image/png");
    }
}
