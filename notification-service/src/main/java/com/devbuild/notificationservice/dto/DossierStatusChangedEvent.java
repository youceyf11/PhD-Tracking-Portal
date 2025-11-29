package com.devbuild.notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DossierStatusChangedEvent {
    private Long dossierId;
    private Long doctorantId;
    private Long directeurId;
    private String ancienStatut;
    private String nouveauStatut;
    private String sujetThese;
    private String commentaire;
    private LocalDateTime timestamp;
    private String eventType;
}