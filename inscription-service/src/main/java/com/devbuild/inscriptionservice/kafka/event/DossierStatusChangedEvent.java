package com.devbuild.inscriptionservice.kafka.event;

import com.devbuild.inscriptionservice.domain.enums.StatutDossier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DossierStatusChangedEvent {


    private Long dossierId;
    private Long doctorantId;
    private Long directeurId;
    private StatutDossier ancienStatut;
    private StatutDossier nouveauStatut;
    private String sujetThese;
    private String commentaire;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    private String eventType; // SUBMISSION, VALIDATION_DIRECTEUR, VALIDATION_ADMIN, REJECTION

    public static DossierStatusChangedEvent forSubmission(Long dossierId, Long doctorantId,
                                                          Long directeurId, String sujetThese) {
        return DossierStatusChangedEvent.builder()
                .dossierId(dossierId)
                .doctorantId(doctorantId)
                .directeurId(directeurId)
                .nouveauStatut(StatutDossier.EN_ATTENTE_DIRECTEUR)
                .sujetThese(sujetThese)
                .eventType("SUBMISSION")
                .build();
    }

    public static DossierStatusChangedEvent forDirecteurValidation(Long dossierId, Long doctorantId,
                                                                   StatutDossier ancienStatut,
                                                                   StatutDossier nouveauStatut,
                                                                   String commentaire) {
        return DossierStatusChangedEvent.builder()
                .dossierId(dossierId)
                .doctorantId(doctorantId)
                .ancienStatut(ancienStatut)
                .nouveauStatut(nouveauStatut)
                .commentaire(commentaire)
                .eventType("VALIDATION_DIRECTEUR")
                .build();
    }

    public static DossierStatusChangedEvent forAdminValidation(Long dossierId, Long doctorantId,
                                                               StatutDossier ancienStatut,
                                                               StatutDossier nouveauStatut,
                                                               String commentaire) {
        return DossierStatusChangedEvent.builder()
                .dossierId(dossierId)
                .doctorantId(doctorantId)
                .ancienStatut(ancienStatut)
                .nouveauStatut(nouveauStatut)
                .commentaire(commentaire)
                .eventType("VALIDATION_ADMIN")
                .build();
    }
}
