package com.devbuild.soutenanceservice.controller;

import com.devbuild.soutenanceservice.domain.dto.request.RapportUploadRequest;
import com.devbuild.soutenanceservice.service.RapportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/soutenances")
@RequiredArgsConstructor
public class RapportController {

    private final RapportService service;

    @PostMapping("/{demandeId}/rapport")
    @PreAuthorize("hasRole('DIRECTEUR')")
    public ResponseEntity<Void> uploadRapport(@PathVariable Long demandeId, @RequestPart("rapport") MultipartFile rapport, @RequestBody RapportUploadRequest request) {
        service.uploadRapport(demandeId, rapport, request.isFavorable());
        return ResponseEntity.ok().build();
    }
}
