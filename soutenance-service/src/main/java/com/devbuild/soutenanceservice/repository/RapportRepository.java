package com.devbuild.soutenanceservice.repository;

import com.devbuild.soutenanceservice.domain.entity.Rapport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RapportRepository extends JpaRepository<Rapport, Long> {
    long countByDemandeSoutenance_Id(Long demandeSoutenanceId);
    long countByDemandeSoutenanceIdAndFavorable(Long demandeSoutenanceId, boolean favorable);
}
