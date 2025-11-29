package com.devbuild.soutenanceservice.domain.dto.response;


import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class DemandeResponse {


    private Long id;
    private Long doctorantId;
    private String manuscritPath;
    private String statut;
    private LocalDate dateSubmission;
    private LocalDate dateSoutenance;
    private LocalTime heureSoutenance;
    private String lieuSoutenance;
    private boolean derrogationDuree;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
