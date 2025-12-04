package com.devbuild.soutenanceservice.controller;

import com.devbuild.soutenanceservice.domain.dto.request.PlanificationRequest;
import com.devbuild.soutenanceservice.domain.dto.response.ApiResponse;
import com.devbuild.soutenanceservice.service.PlanificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/soutenances")
@RequiredArgsConstructor
public class PlanificationController {

    private final PlanificationService service;

    @PostMapping("/{demandeId}/planification")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> planifierSoutenance(
            @PathVariable Long demandeId,
            @RequestBody PlanificationRequest request
    ) {
        service.planifierSoutenance(demandeId, request);
        return ResponseEntity.ok(ApiResponse.success("Soutenance planifiée avec succès", null));
    }
}