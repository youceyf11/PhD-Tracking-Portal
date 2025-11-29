package com.devbuild.inscriptionservice.domain.dto.request;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationDirecteurRequest {

    @NotNull(message = "L'ID du dossier est obligatoire")
    private Long dossierId;

    @NotNull(message = "La décision est obligatoire")
    private Boolean approuve;

    @Size(max = 1000, message = "Le commentaire ne peut pas dépasser 1000 caractères")
    private String commentaire;
}
