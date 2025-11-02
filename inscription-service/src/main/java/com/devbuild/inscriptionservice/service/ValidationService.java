package com.devbuild.inscriptionservice.service;

import com.devbuild.inscriptionservice.domain.dto.response.DossierResponse;
import com.devbuild.inscriptionservice.domain.entity.DossierInscription;
import com.devbuild.inscriptionservice.domain.enums.StatutDossier;
import com.devbuild.inscriptionservice.exception.UnauthorizedException;
import com.devbuild.inscriptionservice.kafka.event.DossierStatusChangedEvent;
import com.devbuild.inscriptionservice.kafka.producer.DossierEventProducer;
import com.devbuild.inscriptionservice.repository.DossierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ValidationService {

    private final DossierRepository dossierRepository;
    private final DossierService dossierService;
    private final DossierEventProducer eventProducer;

    @Transactional
    public DossierResponse validateByDirecteur(Long dossierId, Long directeurId,
                                               Boolean approuve, String commentaire) {

        DossierInscription dossier = dossierService.getDossierEntityById(dossierId);

        // Verify that this directeur is assigned to this dossier
        if (!dossier.getDirecteurId().equals(directeurId)) {
            throw new UnauthorizedException(
                    "Vous n'êtes pas autorisé à valider ce dossier"
            );
        }

        // Verify current status
        if (dossier.getStatut() != StatutDossier.EN_ATTENTE_DIRECTEUR) {
            throw new IllegalStateException(
                    "Ce dossier ne peut pas être validé dans son état actuel: " + dossier.getStatut()
            );
        }

        StatutDossier ancienStatut = dossier.getStatut();
        StatutDossier nouveauStatut;

        if (Boolean.TRUE.equals(approuve)) {
            dossier.setStatut(StatutDossier.EN_ATTENTE_ADMIN);
            nouveauStatut = StatutDossier.EN_ATTENTE_ADMIN;
            log.info("Directeur approved dossier: id={}", dossierId);
        } else {
            dossier.setStatut(StatutDossier.REJETE);
            nouveauStatut = StatutDossier.REJETE;
            log.info("Directeur rejected dossier: id={}", dossierId);
        }

        dossier.setCommentaireDirecteur(commentaire);
        dossier.setDateValidationDirecteur(LocalDateTime.now());

        DossierInscription saved = dossierRepository.save(dossier);

        // Send Kafka event
        DossierStatusChangedEvent event = DossierStatusChangedEvent.forDirecteurValidation(
                dossierId,
                dossier.getDoctorantId(),
                ancienStatut,
                nouveauStatut,
                commentaire
        );
        eventProducer.sendDossierEvent(event);

        return DossierResponse.fromEntity(saved);
    }

    @Transactional
    public DossierResponse validateByAdmin(Long dossierId, Boolean approuve,
                                           String commentaire, Boolean accorderDerogation) {

        DossierInscription dossier = dossierService.getDossierEntityById(dossierId);

        // Verify current status
        if (dossier.getStatut() != StatutDossier.EN_ATTENTE_ADMIN) {
            throw new IllegalStateException(
                    "Ce dossier ne peut pas être validé dans son état actuel: " + dossier.getStatut()
            );
        }

        StatutDossier ancienStatut = dossier.getStatut();
        StatutDossier nouveauStatut;

        if (Boolean.TRUE.equals(approuve)) {
            dossier.setStatut(StatutDossier.VALIDE);
            nouveauStatut = StatutDossier.VALIDE;

            // Grant derogation if requested
            if (Boolean.TRUE.equals(accorderDerogation)) {
                dossier.setDerogationFlag(true);
                log.info("Admin granted derogation for dossier: id={}", dossierId);
            }

            log.info("Admin approved dossier: id={}", dossierId);
        } else {
            dossier.setStatut(StatutDossier.REJETE);
            nouveauStatut = StatutDossier.REJETE;
            log.info("Admin rejected dossier: id={}", dossierId);
        }

        dossier.setCommentaireAdmin(commentaire);
        dossier.setDateValidationAdmin(LocalDateTime.now());

        DossierInscription saved = dossierRepository.save(dossier);

        // Send Kafka event
        DossierStatusChangedEvent event = DossierStatusChangedEvent.forAdminValidation(
                dossierId,
                dossier.getDoctorantId(),
                ancienStatut,
                nouveauStatut,
                commentaire
        );
        eventProducer.sendDossierEvent(event);

        return DossierResponse.fromEntity(saved);
    }
}
