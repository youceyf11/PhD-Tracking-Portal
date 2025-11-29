package com.devbuild.soutenanceservice.domain.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class JuryPropositionRequest {
    private List<String> rapporteurs;
    private List<String> examinateurs;
}
