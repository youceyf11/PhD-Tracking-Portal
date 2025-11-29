package com.devbuild.soutenanceservice.repository;

import com.devbuild.soutenanceservice.domain.entity.Jury;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JuryRepository extends JpaRepository<Jury, Long> {
}
