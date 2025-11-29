package com.devbuild.soutenanceservice.kafka.event;

import lombok.Data;

@Data
public class SoutenanceStatusChangedEvent {

    private Long demandeId;
    private String status;
    private String motif;
}
