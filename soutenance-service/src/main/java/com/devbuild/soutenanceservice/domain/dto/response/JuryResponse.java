package com.devbuild.soutenanceservice.domain.dto.response;


import lombok.Data;

import java.util.List;

@Data
public class JuryResponse {

    private List<String> rapporteurs;
    private List<String> examinateurs;
    private boolean rapportsFavorables;
}
