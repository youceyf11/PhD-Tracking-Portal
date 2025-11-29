package com.devbuild.inscriptionservice.domain.dto.response;


import com.devbuild.inscriptionservice.domain.entity.Campagne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampagneResponse {

    private Long id;
    private String nom;
    private String description;
    private LocalDateTime dateOuverture;
    private LocalDateTime dateFermeture;
    private Boolean active;
    private Boolean enCours;
    private Boolean fermee;
    private Integer nombreDossiers;
    private LocalDateTime createdAt;

    public static CampagneResponse fromEntity(Campagne campagne) {
        return CampagneResponse.builder()
                .id(campagne.getId())
                .nom(campagne.getNom())
                .description(campagne.getDescription())
                .dateOuverture(campagne.getDateOuverture())
                .dateFermeture(campagne.getDateFermeture())
                .active(campagne.getActive())
                .enCours(campagne.isInProgress())
                .fermee(campagne.isClosed())
                .nombreDossiers(campagne.getDossiers() != null ? campagne.getDossiers().size() : 0)
                .createdAt(campagne.getCreatedAt())
                .build();
    }
}
