package com.devbuild.soutenanceservice.service;

import com.devbuild.soutenanceservice.domain.dto.request.PlanificationRequest;
import com.devbuild.soutenanceservice.domain.entity.DemandeSoutenance;
import com.devbuild.soutenanceservice.domain.enums.StatutDemande;
import com.devbuild.soutenanceservice.kafka.producer.SoutenanceEventProducer;
import com.devbuild.soutenanceservice.repository.DemandeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlanificationService {

    private final DemandeRepository repository;
    private final SoutenanceEventProducer eventProducer;

    @Transactional
    public void planifierSoutenance(Long demandeId, PlanificationRequest request) {
        DemandeSoutenance demande = repository.findById(demandeId).orElseThrow();

        if (demande.getStatut() != StatutDemande.AUTORISEE) {
            throw new IllegalStateException("Demande non autorisée");
        }

        demande.setDateSoutenance(request.getDateSoutenance());
        demande.setHeureSoutenance(request.getHeureSoutenance());
        demande.setLieuSoutenance(request.getLieuSoutenance());
        demande.setStatut(StatutDemande.PLANIFIEE);
        repository.save(demande);

        eventProducer.sendSoutenancePlanified(demande.getId());
    }
}
