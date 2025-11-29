package com.devbuild.soutenanceservice.controller;

import com.devbuild.soutenanceservice.domain.dto.request.JuryPropositionRequest;
import com.devbuild.soutenanceservice.service.JuryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/soutenances")
@RequiredArgsConstructor
public class JuryController {

    private final JuryService service;

    @PostMapping("/{demandeId}/jury")
    @PreAuthorize("hasRole('DIRECTEUR')")
    public ResponseEntity<Void> proposeJury(@PathVariable Long demandeId, @RequestBody JuryPropositionRequest request) {
        service.proposeJury(demandeId, request);
        return ResponseEntity.ok().build();
    }
}
