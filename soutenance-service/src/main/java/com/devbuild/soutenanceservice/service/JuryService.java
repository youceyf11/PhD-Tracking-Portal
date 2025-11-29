package com.devbuild.soutenanceservice.service;

import com.devbuild.soutenanceservice.domain.dto.request.JuryPropositionRequest;
import com.devbuild.soutenanceservice.domain.entity.DemandeSoutenance;
import com.devbuild.soutenanceservice.domain.entity.Jury;
import com.devbuild.soutenanceservice.domain.entity.MembreJury;
import com.devbuild.soutenanceservice.domain.enums.StatutDemande;
import com.devbuild.soutenanceservice.domain.enums.TypeMembreJury;
import com.devbuild.soutenanceservice.kafka.producer.SoutenanceEventProducer;
import com.devbuild.soutenanceservice.repository.DemandeRepository;
import com.devbuild.soutenanceservice.repository.JuryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class JuryService {

    private final JuryRepository juryRepository;
    private final DemandeRepository demandeRepository;
    private final SoutenanceEventProducer eventProducer;

    @Transactional
    public void proposeJury(Long demandeId, JuryPropositionRequest request) {
        DemandeSoutenance demande = demandeRepository.findById(demandeId).orElseThrow();

        Jury jury = Jury.builder().build();

        for (String rapporteur : request.getRapporteurs()) {
            MembreJury membre = MembreJury.builder()
                    .nom(rapporteur)
                    .type(TypeMembreJury.RAPPORTEUR)
                    .jury(jury)
                    .build();
            jury.getMembres().add(membre);
        }

        for (String examinateur : request.getExaminateurs()) {
            MembreJury membre = MembreJury.builder()
                    .nom(examinateur)
                    .type(TypeMembreJury.EXAMINATEUR)
                    .jury(jury)
                    .build();
            jury.getMembres().add(membre);
        }

        juryRepository.save(jury);
        demande.setJury(jury);
        demande.setStatut(StatutDemande.EN_ATTENTE_RAPPORTS);
        demandeRepository.save(demande);

        eventProducer.sendJuryProposed(demande.getId());
    }
}
