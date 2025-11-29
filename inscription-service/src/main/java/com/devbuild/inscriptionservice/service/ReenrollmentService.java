package com.devbuild.inscriptionservice.service;

import com.devbuild.inscriptionservice.config.FileStorageProperties;
import com.devbuild.inscriptionservice.domain.dto.response.DossierResponse;
import com.devbuild.inscriptionservice.domain.entity.DossierInscription;
import com.devbuild.inscriptionservice.exception.DureeDepasseeException;
import com.devbuild.inscriptionservice.exception.ResourceNotFoundException;
import com.devbuild.inscriptionservice.repository.DossierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReenrollmentService {

    private final DossierRepository dossierRepository;
    private final FileStorageProperties properties;

    @Transactional(readOnly = true)
    public DossierResponse getPreviousDossierForReenrollment(Long doctorantId) {
        // Get the most recent validated dossier
        DossierInscription previousDossier = dossierRepository
                .findAllByDoctorantIdOrderByDateSubmissionDesc(doctorantId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Aucun dossier précédent trouvé pour la réinscription"
                ));

        return DossierResponse.fromEntity(previousDossier);
    }

    @Transactional(readOnly = true)
    public void validateReenrollmentEligibility(Long doctorantId) {
        // Get initial inscription
        DossierInscription initialDossier = dossierRepository
                .findInitialInscriptionByDoctorantId(doctorantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Aucune inscription initiale trouvée"
                ));

        long anneesDepuisInscription = initialDossier.getAnneesDepuisInscription();
        int dureeInitiale = properties.getDuree().getInitiale();
        int dureeMaximale = properties.getDuree().getMaximum();

        // Check if exceeded initial duration without derogation
        if (anneesDepuisInscription > dureeInitiale && !initialDossier.getDerogationFlag()) {
            throw new DureeDepasseeException(
                    String.format(
                            "La durée initiale du doctorat (%d ans) est dépassée. " +
                                    "Une dérogation administrative est nécessaire pour continuer.",
                            dureeInitiale
                    ),
                    anneesDepuisInscription,
                    false
            );
        }

        // Check if exceeded maximum duration
        if (anneesDepuisInscription > dureeMaximale) {
            throw new DureeDepasseeException(
                    String.format(
                            "La durée maximale du doctorat (%d ans) est dépassée. " +
                                    "Réinscription impossible.",
                            dureeMaximale
                    ),
                    anneesDepuisInscription,
                    true
            );
        }

        // Warning if approaching maximum duration (5+ years)
        if (anneesDepuisInscription >= 5) {
            log.warn("Doctorant {} is approaching maximum duration: {} years",
                    doctorantId, anneesDepuisInscription);
        }
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getReenrollmentStatus(Long doctorantId) {
        Map<String, Object> status = new HashMap<>();

        try {
            DossierInscription initialDossier = dossierRepository
                    .findInitialInscriptionByDoctorantId(doctorantId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Aucune inscription initiale trouvée"
                    ));

            long anneesDepuisInscription = initialDossier.getAnneesDepuisInscription();
            int dureeInitiale = properties.getDuree().getInitiale();
            int dureeMaximale = properties.getDuree().getMaximum();

            status.put("eligible", true);
            status.put("anneesDepuisInscription", anneesDepuisInscription);
            status.put("dureeInitiale", dureeInitiale);
            status.put("dureeMaximale", dureeMaximale);
            status.put("derogationRequired", anneesDepuisInscription > dureeInitiale);
            status.put("hasDerogation", initialDossier.getDerogationFlag());
            status.put("alerteDuree", anneesDepuisInscription >= 5);
            status.put("anneesRestantes", dureeMaximale - anneesDepuisInscription);

            if (anneesDepuisInscription > dureeMaximale) {
                status.put("eligible", false);
                status.put("raison", "Durée maximale dépassée");
            } else if (anneesDepuisInscription > dureeInitiale && !initialDossier.getDerogationFlag()) {
                status.put("eligible", false);
                status.put("raison", "Dérogation administrative requise");
            }

        } catch (ResourceNotFoundException e) {
            status.put("eligible", false);
            status.put("raison", "Aucune inscription initiale trouvée");
        }

        return status;
    }
}
