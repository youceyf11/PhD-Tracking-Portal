package com.devbuild.inscriptionservice.repository;

import com.devbuild.inscriptionservice.domain.entity.Campagne;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CampagneRepository extends JpaRepository<Campagne, Long> {
    Optional<Campagne> findByNom(String nom);
    List<Campagne> findByActiveTrue();

    @Query("SELECT c FROM Campagne c WHERE c.active = true " +
            "AND c.dateOuverture <= :now AND c.dateFermeture >= :now")
    Optional<Campagne> findActiveCampagneInProgress(LocalDateTime now);

    @Query("SELECT c FROM Campagne c WHERE c.dateFermeture < :now")
    List<Campagne> findClosedCampagnes(LocalDateTime now);

    boolean existsByNom(String nom);
}
