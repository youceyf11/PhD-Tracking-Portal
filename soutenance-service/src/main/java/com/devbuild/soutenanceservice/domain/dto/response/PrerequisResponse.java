package com.devbuild.soutenanceservice.domain.dto.response;

import lombok.Data;

@Data
public class PrerequisResponse {

    private int nbArticlesQ1Q2;
    private int nbConferences;
    private int heuresFormation;
    private boolean valide;
}
