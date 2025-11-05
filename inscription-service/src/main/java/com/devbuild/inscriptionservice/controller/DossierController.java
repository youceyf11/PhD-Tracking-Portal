package com.devbuild.inscriptionservice.controller;


import com.devbuild.inscriptionservice.domain.dto.request.DossierSubmissionRequest;
import com.devbuild.inscriptionservice.domain.dto.response.ApiResponse;
import com.devbuild.inscriptionservice.domain.dto.response.DossierResponse;
import com.devbuild.inscriptionservice.domain.enums.TypeDocument;
import com.devbuild.inscriptionservice.service.DossierService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dossiers")
@RequiredArgsConstructor
@Slf4j
public class DossierController {

    private final DossierService dossierService;
    private final ObjectMapper objectMapper;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('DOCTORANT')")
    public ResponseEntity<ApiResponse<DossierResponse>> submitDossier(
            @RequestPart("dossier") String dossierJson,
            @RequestPart(value = "diplome") MultipartFile diplome,
            @RequestPart(value = "cv") MultipartFile cv,
            @RequestPart(value = "lettreMotivation") MultipartFile lettreMotivation,
            @RequestPart(value = "attestation") MultipartFile attestation,
            @RequestPart(value = "autre", required = false) MultipartFile autre,
            Authentication authentication) {

        try {
            Long doctorantId = Long.parseLong(authentication.getName());
            log.info("Submitting dossier for doctorant: {}", doctorantId);

            DossierSubmissionRequest request = objectMapper.readValue(dossierJson, DossierSubmissionRequest.class);

            // Map files to TypeDocument
            Map<TypeDocument, MultipartFile> files = new HashMap<>();
            files.put(TypeDocument.DIPLOME, diplome);
            files.put(TypeDocument.CV, cv);
            files.put(TypeDocument.LETTRE_MOTIVATION, lettreMotivation);
            files.put(TypeDocument.ATTESTATION, attestation);
            if (autre != null) files.put(TypeDocument.AUTRE, autre);

            DossierResponse response = dossierService.submitDossier(request, files, doctorantId);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Dossier soumis avec succès", response));

        } catch (Exception e) {
            log.error("Error submitting dossier", e);
            throw new RuntimeException("Erreur lors de la soumission du dossier: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR', 'DOCTORANT')")
    public ResponseEntity<ApiResponse<DossierResponse>> getDossierById(
            @PathVariable Long id,
            Authentication authentication,
            @RequestHeader("Authorization") String authHeader) {
        log.info("Getting dossier: id={}", id);
        Long userId = Long.parseLong(authentication.getName());
        String token = authHeader.substring(7); // Remove "Bearer "
        DossierResponse response = dossierService.getDossierById(id, userId, token);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/my-dossiers")
    @PreAuthorize("hasRole('DOCTORANT')")
    public ResponseEntity<ApiResponse<List<DossierResponse>>> getMyDossiers(Authentication authentication) {
        Long doctorantId = Long.parseLong(authentication.getName());
        log.info("Getting dossiers for doctorant: {}", doctorantId);
        List<DossierResponse> responses = dossierService.getDossiersByDoctorantId(doctorantId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/directeur/pending")
    @PreAuthorize("hasRole('DIRECTEUR')")
    public ResponseEntity<ApiResponse<List<DossierResponse>>> getPendingDossiersForDirecteur(
            Authentication authentication) {
        Long directeurId = Long.parseLong(authentication.getName());
        log.info("Getting pending dossiers for directeur: {}", directeurId);
        List<DossierResponse> responses = dossierService.getPendingDossiersForDirecteur(directeurId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/admin/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<DossierResponse>>> getPendingDossiersForAdmin() {
        log.info("Getting pending dossiers for admin");
        List<DossierResponse> responses = dossierService.getPendingDossiersForAdmin();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<DossierResponse>>> getAllDossiers() {
        log.info("Getting all dossiers");
        List<DossierResponse> responses = dossierService.getAllDossiers();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/campagne/{campagneId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<DossierResponse>>> getDossiersByCampagne(
            @PathVariable Long campagneId) {
        log.info("Getting dossiers for campagne: {}", campagneId);
        List<DossierResponse> responses = dossierService.getDossiersByCampagneId(campagneId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteDossier(@PathVariable Long id) {
        log.info("Deleting dossier: id={}", id);
        dossierService.deleteDossier(id);
        return ResponseEntity.ok(ApiResponse.success("Dossier supprimé avec succès", null));
    }
}
