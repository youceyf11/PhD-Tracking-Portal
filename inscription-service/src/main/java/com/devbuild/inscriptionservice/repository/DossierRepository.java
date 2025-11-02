package com.devbuild.inscriptionservice.repository;

import com.devbuild.inscriptionservice.domain.entity.DossierInscription;
import com.devbuild.inscriptionservice.domain.enums.StatutDossier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DossierRepository extends  JpaRepository<DossierInscription, Long> {

    List<DossierInscription> findByDoctorantId(Long doctorantId);

    List<DossierInscription> findByDirecteurId(Long directeurId);

    List<DossierInscription> findByStatut(StatutDossier statut);

    @Query("SELECT d FROM DossierInscription d WHERE d.directeurId = :directeurId " +
            "AND d.statut = :statut ORDER BY d.dateSubmission DESC")
    List<DossierInscription> findByDirecteurIdAndStatut(
            @Param("directeurId") Long directeurId,
            @Param("statut") StatutDossier statut
    );

    @Query("SELECT d FROM DossierInscription d WHERE d.doctorantId = :doctorantId " +
            "AND d.campagne.id = :campagneId")
    Optional<DossierInscription> findByDoctorantIdAndCampagneId(
            @Param("doctorantId") Long doctorantId,
            @Param("campagneId") Long campagneId
    );

    @Query("SELECT d FROM DossierInscription d WHERE d.doctorantId = :doctorantId " +
            "AND d.isReenrollment = false ORDER BY d.dateSubmission ASC")
    Optional<DossierInscription> findInitialInscriptionByDoctorantId(
            @Param("doctorantId") Long doctorantId
    );

    @Query("SELECT d FROM DossierInscription d WHERE d.doctorantId = :doctorantId " +
            "ORDER BY d.dateSubmission DESC")
    List<DossierInscription> findAllByDoctorantIdOrderByDateSubmissionDesc(
            @Param("doctorantId") Long doctorantId
    );

    @Query("SELECT COUNT(d) FROM DossierInscription d WHERE d.campagne.id = :campagneId")
    Long countByCampagneId(@Param("campagneId") Long campagneId);

    @Query("SELECT d FROM DossierInscription d WHERE d.derogationFlag = false " +
            "AND YEAR(CURRENT_TIMESTAMP) - YEAR(d.initialInscriptionDate) > 3")
    List<DossierInscription> findDossiersDepassantDureeInitiale();

}
