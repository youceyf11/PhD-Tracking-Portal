package com.devbuild.inscriptionservice.service;


import com.devbuild.inscriptionservice.client.UserServiceClient;
import com.devbuild.inscriptionservice.domain.dto.request.DossierSubmissionRequest;
import com.devbuild.inscriptionservice.domain.dto.response.DossierResponse;
import com.devbuild.inscriptionservice.domain.dto.response.UserResponse;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

        boolean isReenrollment = Boolean.TRUE.equals(request.getIsReenrollment());
        LocalDateTime initialInscriptionDate;
        DossierInscription previousDossier = null;

        if (isReenrollment) {
            // Validate previousDossierId is provided
            if (request.getPreviousDossierId() == null) {
                throw new IllegalArgumentException(
                        "L'ID du dossier précédent est requis pour une réinscription"
                );
            }

            // Get the previous dossier
            previousDossier = dossierRepository.findById(request.getPreviousDossierId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Dossier précédent introuvable avec l'ID: " + request.getPreviousDossierId()
                    ));

            // Verify it belongs to the same doctorant
            if (!previousDossier.getDoctorantId().equals(doctorantId)) {
                throw new IllegalArgumentException(
                        "Le dossier précédent n'appartient pas à ce doctorant"
                );
            }

            initialInscriptionDate = previousDossier.getInitialInscriptionDate();

            long anneesDepuisInscription = java.time.temporal.ChronoUnit.YEARS.between(
                    initialInscriptionDate.toLocalDate(),
                    LocalDateTime.now().toLocalDate()
            );

            log.info("Réinscription - Dossier précédent: id={}, initial date={}, durée: {} ans",
                    previousDossier.getId(), initialInscriptionDate, anneesDepuisInscription);

            if (anneesDepuisInscription > 3) {
                String derogation = previousDossier.getDerogation();
                if (derogation == null || derogation.trim().isEmpty()) {
                    throw new IllegalArgumentException(
                            String.format(
                                    "La durée maximale de la thèse (3 ans) est dépassée (%d ans). " +
                                            "Une dérogation est requise pour poursuivre l'inscription.",
                                    anneesDepuisInscription
                            )
                    );
                }
                log.info("Dérogation trouvée - Durée: {} ans, Motif: {}",
                        anneesDepuisInscription, derogation);
            }
        } else {
            initialInscriptionDate = LocalDateTime.now();
        }

        // Determine actual values to use (new or from previous dossier)
        String sujetThese = isReenrollment &&
                (request.getSujetThese() == null || request.getSujetThese().trim().isEmpty())
                ? previousDossier.getSujetThese()
                : request.getSujetThese();

        Long directeurId = isReenrollment && request.getDirecteurId() == null
                ? previousDossier.getDirecteurId()
                : request.getDirecteurId();

        UserResponse directeur = userServiceClient.validateUserRole(request.getDirecteurId(), "DIRECTEUR");
        if (directeur == null) {
            throw new IllegalArgumentException(
                    "Le directeur spécifié n'existe pas ou n'a pas le rôle DIRECTEUR"
            );
        }
        log.info("Directeur validé: {} {} ({})",
                directeur.getPrenom(),
                directeur.getNom(),
                directeur.getEmail());


        String collaboration = isReenrollment &&
                (request.getCollaboration() == null || request.getCollaboration().trim().isEmpty())
                ? previousDossier.getCollaboration()
                : request.getCollaboration();

        // Create new dossier
        DossierInscription dossier = DossierInscription.builder()
                .doctorantId(doctorantId)
                .sujetThese(sujetThese)
                .directeurId(directeurId)
                .collaboration(collaboration)
                .statut(StatutDossier.EN_ATTENTE_DIRECTEUR)
                .dateSubmission(LocalDateTime.now())
                .initialInscriptionDate(initialInscriptionDate)
                .isReenrollment(isReenrollment)
                .derogationFlag(false)
                .campagne(campagne)
                .build();

        DossierInscription savedDossier = dossierRepository.save(dossier);
        log.info("Dossier created: id={}, doctorantId={}, isReenrollment={}",
                savedDossier.getId(), doctorantId, isReenrollment);

        // Handle documents
        if (isReenrollment && previousDossier != null) {
            // Copy documents from previous dossier if not provided
            for (Document previousDoc : previousDossier.getDocuments()) {
                TypeDocument docType = previousDoc.getTypeDocument();

                // If file is provided for this type, use the new one
                if (files != null && files.containsKey(docType)) {
                    MultipartFile file = files.get(docType);
                    if (file != null && !file.isEmpty()) {
                        saveDocument(savedDossier, docType, file);
                    } else {
                        copyDocument(previousDoc, savedDossier);
                    }
                } else {
                    copyDocument(previousDoc, savedDossier);
                }
            }

            // Add any new document types that weren't in the previous dossier
            if (files != null) {
                for (Map.Entry<TypeDocument, MultipartFile> entry : files.entrySet()) {
                    TypeDocument docType = entry.getKey();
                    boolean existsInPrevious = previousDossier.getDocuments().stream()
                            .anyMatch(doc -> doc.getTypeDocument() == docType);

                    if (!existsInPrevious) {
                        MultipartFile file = entry.getValue();
                        if (file != null && !file.isEmpty()) {
                            saveDocument(savedDossier, docType, file);
                        }
                    }
                }
            }
        } else {
            // First inscription - save all provided files
            if (files != null && !files.isEmpty()) {
                for (Map.Entry<TypeDocument, MultipartFile> entry : files.entrySet()) {
                    MultipartFile file = entry.getValue();
                    if (file != null && !file.isEmpty()) {
                        saveDocument(savedDossier, entry.getKey(), file);
                    }
                }
            }
        }

        // Send Kafka event
        DossierStatusChangedEvent event = DossierStatusChangedEvent.forSubmission(
                savedDossier.getId(),
                doctorantId,
                directeurId,
                sujetThese
        );
        eventProducer.sendDossierEvent(event);

        return DossierResponse.fromEntity(savedDossier);
    }

    private void copyDocument(Document sourceDoc, DossierInscription targetDossier) {
        Document copiedDoc = Document.builder()
                .nomFichier(sourceDoc.getNomFichier())
                .cheminFichier(sourceDoc.getCheminFichier())
                .typeDocument(sourceDoc.getTypeDocument())
                .mimeType(sourceDoc.getMimeType())
                .tailleFichier(sourceDoc.getTailleFichier())
                .dossier(targetDossier)
                .build();

        targetDossier.addDocument(copiedDoc);
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
            String doctorantNom = userServiceClient.getUserFullName(dossier.getDoctorantId());
            String doctorantEmail = userServiceClient.getUserEmail(dossier.getDoctorantId());
            response.setDoctorantNom(doctorantNom);
            response.setDoctorantEmail(doctorantEmail);
        } catch (Exception e) {
            log.warn("Could not fetch doctorant info: {}", e.getMessage());
        }

        // Enrichir avec les infos du directeur
        try {
            String directeurNom = userServiceClient.getUserFullName(dossier.getDirecteurId());
            String directeurEmail = userServiceClient.getUserEmail(dossier.getDirecteurId());
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

    @Transactional(readOnly = true)
    public LocalDate getInitialInscriptionDate(Long doctorantId) {
        Optional<DossierInscription> first = dossierRepository
                .findTopByDoctorantIdOrderByInitialInscriptionDateAsc(doctorantId);

        return first
                .map(DossierInscription::getInitialInscriptionDate)
                .map(LocalDateTime::toLocalDate)
                .orElse(null);
    }
}
