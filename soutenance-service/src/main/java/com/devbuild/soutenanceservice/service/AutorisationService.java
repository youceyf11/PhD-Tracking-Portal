package com.devbuild.soutenanceservice.service;

import com.devbuild.soutenanceservice.domain.entity.DemandeSoutenance;
import com.devbuild.soutenanceservice.domain.enums.StatutDemande;
import com.devbuild.soutenanceservice.kafka.producer.SoutenanceEventProducer;
import com.devbuild.soutenanceservice.repository.DemandeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AutorisationService {

    private final DemandeRepository repository;
    private final SoutenanceEventProducer eventProducer;

    @Transactional
    public void autoriserSoutenance(Long demandeId) {
        DemandeSoutenance demande = repository.findById(demandeId).orElseThrow();

        if (!demande.getJury().isRapportsFavorables()) {
            throw new IllegalStateException("Rapports non favorables");
        }

        demande.setStatut(StatutDemande.AUTORISEE);
        repository.save(demande);

        eventProducer.sendSoutenanceAuthorisee(demande.getId());
    }
}
