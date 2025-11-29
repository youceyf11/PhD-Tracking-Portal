package com.devbuild.inscriptionservice.service;


import com.devbuild.inscriptionservice.domain.dto.request.CampagneRequest;
import com.devbuild.inscriptionservice.domain.dto.response.CampagneResponse;
import com.devbuild.inscriptionservice.domain.entity.Campagne;
import com.devbuild.inscriptionservice.exception.CampagneNotActiveException;
import com.devbuild.inscriptionservice.repository.CampagneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class CampagneService {

    private final CampagneRepository campagneRepository;

    public CampagneResponse createCampagne(CampagneRequest request ){
        request.validate();

        if(campagneRepository.existsByNom(request.getNom())){
            throw new IllegalArgumentException("Campagne exists already with name " + request.getNom());
        }

        Campagne campagne= Campagne.builder()
                .nom(request.getNom())
                .description(request.getDescription())
                .dateOuverture(request.getDateOuverture())
                .dateFermeture(request.getDateFermeture())
                .active(request.getActive())
                .build();
        Campagne savedCampagne = campagneRepository.save(campagne);
        log.info(" campagne created with id {}, nom{} ", savedCampagne.getId(), savedCampagne.getNom());
        return CampagneResponse.fromEntity(savedCampagne);
    }

    public CampagneResponse updateCampagne(Long id, CampagneRequest request){
        request.validate();

        Campagne campagne = campagneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campagne not found with ID: " + id));

        if(!campagne.getNom().equals(request.getNom()) && campagneRepository.existsByNom(request.getNom())){
            throw new IllegalArgumentException("Campagne exists already with name " + request.getNom());
        }

        campagne.setNom(request.getNom());
        campagne.setDescription(request.getDescription());
        campagne.setDateOuverture(request.getDateOuverture());
        campagne.setDateFermeture(request.getDateFermeture());
        campagne.setActive(request.getActive());
        Campagne updatedCampagne = campagneRepository.save(campagne);
        log.info("Campagne updated with id{}", updatedCampagne.getId());

        return CampagneResponse.fromEntity(updatedCampagne);

    }

    @Transactional(readOnly = true)
    public CampagneResponse getCampagneById(Long id){
        Campagne campagne= campagneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campagne not found with ID: " + id));
        return CampagneResponse.fromEntity(campagne);
    }

    @Transactional(readOnly = true)
    public List<CampagneResponse> getAllCampagnes(){
        return campagneRepository.findAll().stream()
                .map(CampagneResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CampagneResponse> getActiveCampagnes() {
        return campagneRepository.findByActiveTrue().stream()
                .map(CampagneResponse::fromEntity)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public CampagneResponse getCurrentActiveCampagne() {
        Campagne campagne = campagneRepository.findActiveCampagneInProgress(LocalDateTime.now())
                .orElseThrow(() -> new CampagneNotActiveException("Aucune campagne active en cours"));
        return CampagneResponse.fromEntity(campagne);
    }


    public void deleteCampagne(Long id) {
        Campagne campagne = campagneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campagne not found with ID: " + id));

        if (!campagne.getDossiers().isEmpty()) {
            throw new IllegalStateException("Impossible de supprimer une campagne contenant des dossiers");
        }

        campagneRepository.delete(campagne);
        log.info("Campagne deleted: id={}", id);
    }

    @Transactional(readOnly = true)
    public Campagne getCampagneEntityById(Long id) {
        return campagneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campagne non trouvée avec l'ID: " + id));
    }


    public void validateCampagneIsActive(Campagne campagne) {
        if (!campagne.isInProgress()) {
            throw new CampagneNotActiveException(
                    "La campagne n'est pas active ou est en dehors de ses dates d'ouverture/fermeture"
            );
        }
    }
}
