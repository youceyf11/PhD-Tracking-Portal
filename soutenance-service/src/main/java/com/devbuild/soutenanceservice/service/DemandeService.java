package com.devbuild.soutenanceservice.service;

import com.devbuild.soutenanceservice.domain.dto.request.DemandeSubmissionRequest;
import com.devbuild.soutenanceservice.domain.dto.response.DemandeResponse;
import com.devbuild.soutenanceservice.domain.entity.DemandeSoutenance;
import com.devbuild.soutenanceservice.domain.entity.DocumentSoutenance;
import com.devbuild.soutenanceservice.domain.entity.Prerequis;
import com.devbuild.soutenanceservice.domain.enums.StatutDemande;
import com.devbuild.soutenanceservice.domain.enums.TypeDocumentSoutenance;
import com.devbuild.soutenanceservice.kafka.producer.SoutenanceEventProducer;
import com.devbuild.soutenanceservice.repository.DemandeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DemandeService {

    private final DemandeRepository repository;
    private final PrerequisService prerequisService;
    private final FileStorageService fileStorageService;
    private final SoutenanceEventProducer eventProducer;
    private final DureeService dureeService;


    @Transactional
    public DemandeResponse submitDemande(DemandeSubmissionRequest demandeSubmissionRequest,
                                         MultipartFile manuscrit,
                                         List<MultipartFile> documents,
                                         Long doctorantId) {

        // 1. STEP ONE: Check Duration Eligibility FIRST (Fail fast)
        // If the student has exceeded the max duration without derogation, stop here.
        // We pass 'false' for derogation on initial submission (unless provided in request).
        dureeService.validerEligibilite(doctorantId, false);

        // 2. STEP TWO: Build and Validate Prerequisites
        Prerequis prerequis = Prerequis.builder()
                .nbArticlesQ1Q2(demandeSubmissionRequest.getNbArticlesQ1Q2())
                .nbConferences(demandeSubmissionRequest.getNbConferences())
                .heuresFormation(demandeSubmissionRequest.getHeuresFormation())
                .build();

        // This throws exception if conditions aren't met
        prerequisService.validatePrerequis(prerequis);

        // 3. STEP THREE: Initialize Demande Entity
        DemandeSoutenance demande = DemandeSoutenance.builder()
                .doctorantId(doctorantId)
                .dateSubmission(LocalDate.now())
                .statut(StatutDemande.EN_ATTENTE_PREREQUIS)
                .prerequis(prerequis)
                .build();

        // 4. STEP FOUR: File Storage (Heavy I/O operation performed only if valid)
        String manuscritPath = fileStorageService.storeFile(manuscrit, doctorantId);
        demande.setManuscritPath(manuscritPath);

        if (documents != null) {
            for (MultipartFile file : documents) {
                String path = fileStorageService.storeFile(file, doctorantId);
                DocumentSoutenance document = DocumentSoutenance.builder()
                        .path(path)
                        .type(TypeDocumentSoutenance.DEMANDE_MANUSCRITE)
                        .demandeSoutenance(demande)
                        .build();
                demande.getDocuments().add(document);
            }
        }

        // 5. STEP FIVE: Save to Database
        demande = repository.save(demande);

        // 6. STEP SIX: Check for Alerts (Must be done AFTER save to have the ID)
        // This sends an alert event if the student is between 5 and 6 years
        dureeService.verifierAlerte(demande.getId(), doctorantId);

        // 7. STEP SEVEN: Send Submission Event
        eventProducer.sendDemandeSubmitted(demande.getId());

        return mapToResponse(demande);
    }

    public DemandeResponse getDemandeStatus(Long demandeId) {
        DemandeSoutenance demande = repository.findById(demandeId).orElseThrow();
        return mapToResponse(demande);
    }

    private DemandeResponse mapToResponse(DemandeSoutenance demande) {
        DemandeResponse response = new DemandeResponse();
        response.setId(demande.getId());
        response.setDoctorantId(demande.getDoctorantId());
        response.setManuscritPath(demande.getManuscritPath());
        response.setStatut(demande.getStatut().name());
        response.setDateSubmission(demande.getDateSubmission());
        response.setDateSoutenance(demande.getDateSoutenance());
        response.setHeureSoutenance(demande.getHeureSoutenance());
        response.setLieuSoutenance(demande.getLieuSoutenance());
        response.setDerrogationDuree(demande.isDerrogationDuree());
        response.setCreatedAt(demande.getCreatedAt());
        response.setUpdatedAt(demande.getUpdatedAt());
        return response;
    }



    public List<DemandeResponse> getAllDemandes() {
        return repository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<DemandeResponse> getDemandesByDoctorant(Long doctorantId) {
        return repository.findByDoctorantId(doctorantId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void validerPrerequis(Long demandeId) {
        DemandeSoutenance demande = repository.findById(demandeId)
                .orElseThrow(() -> new RuntimeException("Demande introuvable"));

        // Vérification de cohérence
        if (demande.getStatut() != StatutDemande.EN_ATTENTE_PREREQUIS) {
            throw new IllegalStateException("Impossible de valider : statut incorrect");
        }

        // Passage à l'étape suivante
        demande.setStatut(StatutDemande.EN_ATTENTE_JURY);
        repository.save(demande);

    }
}
