package com.devbuild.notificationservice.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class DemandeInfo {
    private Long id;
    private Long doctorantId;
    private String statut;
    private LocalDate dateSoutenance;
    private LocalTime heureSoutenance;
    private String lieuSoutenance;
}