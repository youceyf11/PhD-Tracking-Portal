package com.devbuild.inscriptionservice.service;


import com.devbuild.inscriptionservice.client.UserServiceClient;
import com.devbuild.inscriptionservice.domain.dto.request.DossierSubmissionRequest;
import com.devbuild.inscriptionservice.domain.dto.response.DossierResponse;
import com.devbuild.inscriptionservice.domain.entity.Campagne;
import com.devbuild.inscriptionservice.domain.entity.Document;
import com.devbuild.inscriptionservice.domain.entity.DossierInscription;
import com.devbuild.inscriptionservice.domain.enums.StatutDossier;
import com.devbuild.inscriptionservice.domain.enums.TypeDocument;
import com.devbuild.inscriptionservice.exception.ResourceNotFoundException;
import com.devbuild.inscriptionservice.kafka.event.DossierStatusChangedEvent;
import com.devbuild.inscriptionservice.kafka.producer.DossierEventProducer;
import com.devbuild.inscriptionservice.repository.DossierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DossierService {

    private final DossierRepository dossierRepository;
    private final CampagneService campagneService;
    private final FileStorageService fileStorageService;
    private final DossierEventProducer eventProducer;
    private final UserServiceClient userServiceClient;
    private final Tika tika;

    @Transactional
    public DossierResponse submitDossier(DossierSubmissionRequest request,
                                         Map<TypeDocument, MultipartFile> files,
                                         Long doctorantId) {

        // Validate campagne is active
        Campagne campagne = campagneService.getCampagneEntityById(request.getCampagneId());
        campagneService.validateCampagneIsActive(campagne);

        // Check if doctorant already submitted for this campagne
        dossierRepository.findByDoctorantIdAndCampagneId(doctorantId, campagne.getId())
                .ifPresent(existing -> {
                    throw new IllegalStateException(
                            "Vous avez déjà soumis un dossier pour cette campagne"
                    );
                });

        // Determine if this is a reenrollment
        boolean isReenrollment = Boolean.TRUE.equals(request.getIsReenrollment());
        LocalDateTime initialInscriptionDate;

        if (isReenrollment) {
            // Get the initial inscription date from the first dossier
            DossierInscription initialDossier = dossierRepository
                    .findInitialInscriptionByDoctorantId(doctorantId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Aucune inscription initiale trouvée pour la réinscription"
                    ));
            initialInscriptionDate = initialDossier.getInitialInscriptionDate();
        } else {
            initialInscriptionDate = LocalDateTime.now();
        }

        // Create dossier
        DossierInscription dossier = DossierInscription.builder()
                .doctorantId(doctorantId)
                .sujetThese(request.getSujetThese())
                .directeurId(request.getDirecteurId())
                .collaboration(request.getCollaboration())
                .statut(StatutDossier.EN_ATTENTE_DIRECTEUR)
                .dateSubmission(LocalDateTime.now())
                .initialInscriptionDate(initialInscriptionDate)
                .isReenrollment(isReenrollment)
                .derogationFlag(false)
                .campagne(campagne)
                .build();

        DossierInscription savedDossier = dossierRepository.save(dossier);
        log.info("Dossier created: id={}, doctorantId={}", savedDossier.getId(), doctorantId);

        // Save files
        if (files != null && !files.isEmpty()) {
            for (Map.Entry<TypeDocument, MultipartFile> entry : files.entrySet()) {
                MultipartFile file = entry.getValue();
                if (file != null && !file.isEmpty()) {
                    saveDocument(savedDossier, entry.getKey(), file);
                }
            }
        }

        // Send Kafka event
        DossierStatusChangedEvent event = DossierStatusChangedEvent.forSubmission(
                savedDossier.getId(),
                doctorantId,
                request.getDirecteurId(),
                request.getSujetThese()
        );
        eventProducer.sendDossierEvent(event);

        return DossierResponse.fromEntity(savedDossier);
    }

    private void saveDocument(DossierInscription dossier, TypeDocument typeDocument, MultipartFile file) {
        try {
            String filename = fileStorageService.storeFile(file, dossier.getId());
            String mimeType = tika.detect(file.getInputStream());

            Document document = Document.builder()
                    .nomFichier(file.getOriginalFilename())
                    .cheminFichier(filename)
                    .typeDocument(typeDocument)
                    .mimeType(mimeType)
                    .tailleFichier(file.getSize())
                    .dossier(dossier)
                    .build();

            dossier.addDocument(document);
            log.info("Document added to dossier: type={}, filename={}", typeDocument, filename);

        } catch (Exception e) {
            log.error("Failed to save document", e);
            throw new RuntimeException("Erreur lors de la sauvegarde du document", e);
        }
    }

    @Transactional(readOnly = true)
    public DossierResponse getDossierById(Long id, Long userId, String authToken) {
        DossierInscription dossier = dossierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dossier non trouvé avec l'ID: " + id));

        // Check authorization
        if (!dossier.getDoctorantId().equals(userId) &&
                !dossier.getDirecteurId().equals(userId)) {
            log.debug("Admin accessing dossier: {}", id);
        }

        return enrichDossierResponse(dossier, authToken);
    }

    /**
     * Enrichit la réponse avec les informations des utilisateurs depuis user-service
     */
    private DossierResponse enrichDossierResponse(DossierInscription dossier, String authToken) {
        DossierResponse response = DossierResponse.fromEntity(dossier);

        // Enrichir avec les infos du doctorant
        try {
            String doctorantNom = userServiceClient.getUserFullName(dossier.getDoctorantId(), authToken);
            String doctorantEmail = userServiceClient.getUserEmail(dossier.getDoctorantId(), authToken);
            response.setDoctorantNom(doctorantNom);
            response.setDoctorantEmail(doctorantEmail);
        } catch (Exception e) {
            log.warn("Could not fetch doctorant info: {}", e.getMessage());
        }

        // Enrichir avec les infos du directeur
        try {
            String directeurNom = userServiceClient.getUserFullName(dossier.getDirecteurId(), authToken);
            String directeurEmail = userServiceClient.getUserEmail(dossier.getDirecteurId(), authToken);
            response.setDirecteurNom(directeurNom);
            response.setDirecteurEmail(directeurEmail);
        } catch (Exception e) {
            log.warn("Could not fetch directeur info: {}", e.getMessage());
        }

        return response;
    }

    @Transactional(readOnly = true)
    public List<DossierResponse> getDossiersByDoctorantId(Long doctorantId) {
        return dossierRepository.findAllByDoctorantIdOrderByDateSubmissionDesc(doctorantId)
                .stream()
                .map(DossierResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DossierResponse> getPendingDossiersForDirecteur(Long directeurId) {
        return dossierRepository.findByDirecteurIdAndStatut(directeurId, StatutDossier.EN_ATTENTE_DIRECTEUR)
                .stream()
                .map(DossierResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DossierResponse> getPendingDossiersForAdmin() {
        return dossierRepository.findByStatut(StatutDossier.EN_ATTENTE_ADMIN)
                .stream()
                .map(DossierResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DossierResponse> getAllDossiers() {
        return dossierRepository.findAll()
                .stream()
                .map(DossierResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DossierResponse> getDossiersByCampagneId(Long campagneId) {
        Campagne campagne = campagneService.getCampagneEntityById(campagneId);
        return campagne.getDossiers()
                .stream()
                .map(DossierResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteDossier(Long id) {
        DossierInscription dossier = dossierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dossier non trouvé avec l'ID: " + id));

        // Delete associated files
        dossier.getDocuments().forEach(doc ->
                fileStorageService.deleteFile(doc.getCheminFichier())
        );

        dossierRepository.delete(dossier);
        log.info("Dossier deleted: id={}", id);
    }

    @Transactional(readOnly = true)
    public DossierInscription getDossierEntityById(Long id) {
        return dossierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dossier non trouvé avec l'ID: " + id));
    }
}
