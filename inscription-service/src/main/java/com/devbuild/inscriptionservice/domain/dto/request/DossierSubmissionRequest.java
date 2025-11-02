package com.devbuild.inscriptionservice.domain.dto.request;

import com.devbuild.inscriptionservice.domain.enums.TypeDocument;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class DossierSubmissionRequest {

    @NotNull(message = "L'ID de la campagne est obligatoire")
    private Long campagneId;

    @NotBlank(message = "Le sujet de thèse est obligatoire")
    @Size(min = 10, max = 1000, message = "Le sujet doit contenir entre 10 et 1000 caractères")
    private String sujetThese;

    @NotNull(message = "L'ID du directeur de thèse est obligatoire")
    private Long directeurId;

    @Size(max = 500, message = "La collaboration ne peut pas dépasser 500 caractères")
    private String collaboration;

    // Map of TypeDocument to file identifier (will be handled with MultipartFile separately)
    private Map<TypeDocument, String> documentsTypes;

    // For reenrollment
    private Boolean isReenrollment;
    private Long previousDossierId;
}
