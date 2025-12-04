package com.devbuild.soutenanceservice.controller;

import com.devbuild.soutenanceservice.domain.dto.request.DemandeSubmissionRequest;
import com.devbuild.soutenanceservice.domain.dto.response.ApiResponse;
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
    public ResponseEntity<ApiResponse<DemandeResponse>> submitDemande(
            @RequestPart("request") DemandeSubmissionRequest request,
            @RequestPart("manuscrit") MultipartFile manuscrit,
            @RequestPart("documents") List<MultipartFile> documents,
            Authentication authentication) {

        Long doctorantId = Long.parseLong(authentication.getName());
        DemandeResponse response = demandeService.submitDemande(request, manuscrit, documents, doctorantId);


        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{demandeId}")
    public ResponseEntity<ApiResponse<DemandeResponse>> getDemandeStatus(@PathVariable Long demandeId) {
        DemandeResponse response = demandeService.getDemandeStatus(demandeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/my-demandes")
    @PreAuthorize("hasRole('DOCTORANT')")
    public ResponseEntity<ApiResponse<List<DemandeResponse>>> getMyDemandes(Authentication authentication) {
        Long doctorantId = Long.parseLong(authentication.getName());
        List<DemandeResponse> responses = demandeService.getDemandesByDoctorant(doctorantId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR')")
    public ResponseEntity<ApiResponse<List<DemandeResponse>>> getAllDemandes() {
        List<DemandeResponse> responses = demandeService.getAllDemandes();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/doctorant/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR')")
    public ResponseEntity<ApiResponse<List<DemandeResponse>>> getDemandesByDoctorantId(@PathVariable Long id) {
        List<DemandeResponse> responses = demandeService.getDemandesByDoctorant(id);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PostMapping("/{demandeId}/valider-prerequis")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> validerPrerequis(@PathVariable Long demandeId) {
        demandeService.validerPrerequis(demandeId);
        return ResponseEntity.ok(ApiResponse.success("Prérequis validés. Le dossier est transmis au directeur.", null));
    }
}