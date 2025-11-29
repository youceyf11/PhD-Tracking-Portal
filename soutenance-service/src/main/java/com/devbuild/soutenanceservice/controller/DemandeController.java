package com.devbuild.soutenanceservice.controller;

import com.devbuild.soutenanceservice.domain.dto.request.DemandeSubmissionRequest;
import com.devbuild.soutenanceservice.domain.dto.response.DemandeResponse;
import com.devbuild.soutenanceservice.service.DemandeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/soutenances")
@RequiredArgsConstructor
public class DemandeController {

    private final DemandeService demandeService;

    @PostMapping(value = "/submit", consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('DOCTORANT')")
    public ResponseEntity<DemandeResponse> submitDemande(
            @RequestPart("request") DemandeSubmissionRequest request,
            @RequestPart("manuscrit") MultipartFile manuscrit,
            @RequestPart("documents") List<MultipartFile> documents,
            Authentication authentication) {

        Long doctorantId = Long.parseLong(authentication.getName());

        return ResponseEntity.ok(demandeService.submitDemande(request, manuscrit, documents, doctorantId));
    }

    @GetMapping("/{demandeId}/status")
    public ResponseEntity<DemandeResponse> getDemandeStatus(@PathVariable Long demandeId) {
        return ResponseEntity.ok(demandeService.getDemandeStatus(demandeId));
    }
}