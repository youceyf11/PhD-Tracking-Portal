package com.devbuild.soutenanceservice.repository;

import com.devbuild.soutenanceservice.domain.entity.DemandeSoutenance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DemandeRepository extends JpaRepository<DemandeSoutenance, Long> {
    List<DemandeSoutenance> findByDoctorantId(Long doctorantId);
}
