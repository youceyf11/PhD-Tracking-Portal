package com.devbuild.inscriptionservice.domain.dto.request;


import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class CampagneRequest {

    @NotBlank(message = "Le nom de la campagne est obligatoire")
    @Size(min = 3, max = 100, message = "Le nom doit contenir entre 3 et 100 caractères")
    private String nom;

    @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères")
    private String description;

    @NotNull(message = "La date d'ouverture est obligatoire")
    private LocalDateTime dateOuverture;

    @NotNull(message = "La date de fermeture est obligatoire")
    @Future(message = "La date de fermeture doit être dans le futur")
    private LocalDateTime dateFermeture;

    @NotNull(message = "Le statut actif est obligatoire")
    private Boolean active;

    public void validate() {
        if (dateFermeture != null && dateOuverture != null) {
            if (dateFermeture.isBefore(dateOuverture)) {
                throw new IllegalArgumentException(
                        "La date de fermeture doit être après la date d'ouverture"
                );
            }
        }
    }
}