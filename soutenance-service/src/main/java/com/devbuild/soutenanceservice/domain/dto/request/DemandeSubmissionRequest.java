package com.devbuild.soutenanceservice.domain.dto.request;

import lombok.Data;

@Data
public class DemandeSubmissionRequest {
    private int nbArticlesQ1Q2;
    private int nbConferences;
    private int heuresFormation;
}
