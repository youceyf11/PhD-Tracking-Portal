package com.devbuild.soutenanceservice.controller;

import com.devbuild.soutenanceservice.domain.dto.request.RapportUploadRequest;
import com.devbuild.soutenanceservice.domain.dto.response.ApiResponse;
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
    public ResponseEntity<ApiResponse<Void>> uploadRapport(@PathVariable Long demandeId, @RequestPart("rapport") MultipartFile rapport, @RequestPart("request") RapportUploadRequest request) {
        service.uploadRapport(demandeId, rapport, request.isFavorable());
        return ResponseEntity.ok(ApiResponse.success("Rapport déposé avec succès", null));
    }
}
