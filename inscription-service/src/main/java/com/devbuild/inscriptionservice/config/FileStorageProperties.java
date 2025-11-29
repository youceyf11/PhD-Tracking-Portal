package com.devbuild.inscriptionservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "dossier")
@Data
public class FileStorageProperties {

    private Duree duree = new Duree();
    private File file = new File();
    private Storage storage = new Storage();

    @Data
    public static class Duree {
        private int initiale = 3;
        private int maximum = 6;
    }

    @Data
    public static class File {
        private List<String> allowedExtensions = List.of("pdf", "jpg", "jpeg", "png");
        private long maxSize = 5242880; // 5MB
    }

    @Data
    public static class Storage {
        private String path = "./uploads/dossiers";
    }
}
