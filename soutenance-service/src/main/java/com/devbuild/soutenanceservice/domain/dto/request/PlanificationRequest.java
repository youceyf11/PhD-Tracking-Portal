package com.devbuild.soutenanceservice.domain.dto.request;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class PlanificationRequest {

    private LocalDate dateSoutenance;
    private LocalTime heureSoutenance;
    private String lieuSoutenance;

}
