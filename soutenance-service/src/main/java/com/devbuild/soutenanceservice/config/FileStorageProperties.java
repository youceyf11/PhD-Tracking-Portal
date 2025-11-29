package com.devbuild.soutenanceservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
@ConfigurationProperties(prefix = "file")
public class FileStorageProperties {
    private String uploadDir = "uploads/soutenances";
}
