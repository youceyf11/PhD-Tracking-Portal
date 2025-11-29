package com.devbuild.soutenanceservice.service;

import com.devbuild.soutenanceservice.client.InscriptionServiceClient;
import com.devbuild.soutenanceservice.exception.DureeDepasseeException;
import com.devbuild.soutenanceservice.kafka.producer.SoutenanceEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class DureeService {

    private final InscriptionServiceClient inscriptionClient;
    private final SoutenanceEventProducer soutenanceEventProducer;

    /**
     * Called BEFORE saving the demand.
     * Blocks the process if duration > 6 years and no derogation.
     */
    public void validerEligibilite(Long doctorantId, boolean hasDerogation) {
        LocalDate initialDate = inscriptionClient.getInitialInscriptionDate(doctorantId);

        if (initialDate == null) {
            log.warn("Date d'inscription initiale introuvable pour le doctorant {}", doctorantId);
            return; // Or throw exception depending on business rules
        }

        long years = ChronoUnit.YEARS.between(initialDate, LocalDate.now());

        log.info("Vérification durée pour doctorant {}: {} ans", doctorantId, years);

        if (years > 6 && !hasDerogation) {
            throw new DureeDepasseeException("Durée du doctorat (" + years + " ans) dépassée. Limite: 6 ans.");
        }
    }

    /**
     * Called AFTER saving the demand.
     * Sends an alert event if duration > 5 years.
     */
    public void verifierAlerte(Long demandeId, Long doctorantId) {
        LocalDate initialDate = inscriptionClient.getInitialInscriptionDate(doctorantId);

        if (initialDate == null) return;

        long years = ChronoUnit.YEARS.between(initialDate, LocalDate.now());

        if (years >= 5) {
            log.warn("Alerte: Le doctorant {} approche de la date limite ({} ans)", doctorantId, years);
            soutenanceEventProducer.sendDureeAlerte(demandeId);
        }
    }
}