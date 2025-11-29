package com.devbuild.inscriptionservice.domain.dto.response;

import com.devbuild.inscriptionservice.domain.entity.Document;
import com.devbuild.inscriptionservice.domain.entity.DossierInscription;
import com.devbuild.inscriptionservice.domain.enums.StatutDossier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DossierResponse {

    private Long id;
    private Long doctorantId;
    private String doctorantNom;
    private String doctorantEmail;
    private String sujetThese;
    private Long directeurId;
    private String directeurNom;
    private String directeurEmail;
    private String collaboration;
    private StatutDossier statut;
    private String statutDescription;
    private LocalDateTime dateSubmission;
    private LocalDateTime initialInscriptionDate;
    private Long anneesDepuisInscription;
    private Boolean derogationFlag;
    private String commentaireDirecteur;
    private String commentaireAdmin;
    private LocalDateTime dateValidationDirecteur;
    private LocalDateTime dateValidationAdmin;
    private Boolean isReenrollment;
    private Long campagneId;
    private String campagneNom;
    private List<DocumentInfo> documents;
    private Boolean alerteDuree;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    public static class DocumentInfo {
        private Long id;
        private String nomFichier;
        private String typeDocument;
        private String mimeType;
        private Long tailleFichier;
        private LocalDateTime uploadedAt;
    }

    public static DossierResponse fromEntity(DossierInscription dossier) {
        return DossierResponse.builder()
                .id(dossier.getId())
                .doctorantId(dossier.getDoctorantId())
                .sujetThese(dossier.getSujetThese())
                .directeurId(dossier.getDirecteurId())
                .collaboration(dossier.getCollaboration())
                .statut(dossier.getStatut())
                .statutDescription(dossier.getStatut().getDescription())
                .dateSubmission(dossier.getDateSubmission())
                .initialInscriptionDate(dossier.getInitialInscriptionDate())
                .anneesDepuisInscription(dossier.getAnneesDepuisInscription())
                .derogationFlag(dossier.getDerogationFlag())
                .commentaireDirecteur(dossier.getCommentaireDirecteur())
                .commentaireAdmin(dossier.getCommentaireAdmin())
                .dateValidationDirecteur(dossier.getDateValidationDirecteur())
                .dateValidationAdmin(dossier.getDateValidationAdmin())
                .isReenrollment(dossier.getIsReenrollment())
                .campagneId(dossier.getCampagne().getId())
                .campagneNom(dossier.getCampagne().getNom())
                .documents(dossier.getDocuments().stream()
                        .map(DossierResponse::mapDocument)
                        .collect(Collectors.toList()))
                .alerteDuree(dossier.approcheLimiteMaximale())
                .createdAt(dossier.getCreatedAt())
                .updatedAt(dossier.getUpdatedAt())
                .build();
    }

    private static DocumentInfo mapDocument(Document doc) {
        return DocumentInfo.builder()
                .id(doc.getId())
                .nomFichier(doc.getNomFichier())
                .typeDocument(doc.getTypeDocument().name())
                .mimeType(doc.getMimeType())
                .tailleFichier(doc.getTailleFichier())
                .uploadedAt(doc.getUploadedAt())
                .build();
    }
}