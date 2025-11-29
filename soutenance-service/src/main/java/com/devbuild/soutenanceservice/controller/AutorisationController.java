package com.devbuild.soutenanceservice.controller;

import com.devbuild.soutenanceservice.domain.dto.request.AutorisationRequest;
import com.devbuild.soutenanceservice.service.AutorisationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/soutenances")
public class AutorisationController {

    private final AutorisationService service;

    @PostMapping("/{demandeId}/autorisation")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> autoriserSoutenance(@PathVariable Long demandeId, @RequestBody AutorisationRequest request) {
        service.autoriserSoutenance(demandeId);
        return ResponseEntity.ok().build();
    }
}
