package com.devbuild.soutenanceservice.service;

import com.devbuild.soutenanceservice.domain.entity.DemandeSoutenance;
import com.devbuild.soutenanceservice.domain.entity.Rapport;
import com.devbuild.soutenanceservice.domain.enums.StatutDemande;
import com.devbuild.soutenanceservice.kafka.producer.SoutenanceEventProducer;
import com.devbuild.soutenanceservice.repository.DemandeRepository;
import com.devbuild.soutenanceservice.repository.RapportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class RapportService {

    private final RapportRepository repository;
    private final DemandeRepository demandeRepository;
    private final FileStorageService fileStorageService;
    private final SoutenanceEventProducer eventProducer;

    @Transactional
    public void uploadRapport(Long demandeId, MultipartFile file, boolean favorable) {
        DemandeSoutenance demande = demandeRepository.findById(demandeId).orElseThrow();

        String path = fileStorageService.storeFile(file, demande.getDoctorantId());

        Rapport rapport = Rapport.builder()
                .path(path)
                .favorable(favorable)
                .demandeSoutenance(demande)
                .build();

        repository.save(rapport);

        long nbRapports = repository.countByDemandeSoutenance_Id(demande.getId());
        long nbFavorables = repository.countByDemandeSoutenanceIdAndFavorable(demande.getId(), true);

        if (nbFavorables == nbRapports) {
            demande.getJury().setRapportsFavorables(true);
            demande.setStatut(StatutDemande.AUTORISEE);
            demandeRepository.save(demande);
            eventProducer.sendRapportsOk(demande.getId());
        }
    }

}
