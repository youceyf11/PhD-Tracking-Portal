package com.devbuild.inscriptionservice.controller;


import com.devbuild.inscriptionservice.domain.dto.request.DossierSubmissionRequest;
import com.devbuild.inscriptionservice.domain.dto.response.ApiResponse;
import com.devbuild.inscriptionservice.domain.dto.response.DossierResponse;
import com.devbuild.inscriptionservice.domain.enums.TypeDocument;
import com.devbuild.inscriptionservice.service.DossierService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dossiers")
@RequiredArgsConstructor
@Slf4j
public class DossierController {

    private final DossierService dossierService;


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('DOCTORANT')")
    public ResponseEntity<ApiResponse<DossierResponse>> submitDossier(
            @RequestPart("dossier") @Valid DossierSubmissionRequest request,

            @RequestPart(value = "diplome", required = false) MultipartFile diplome,
            @RequestPart(value = "cv", required = false) MultipartFile cv,
            @RequestPart(value = "lettreMotivation", required = false) MultipartFile lettreMotivation,
            @RequestPart(value = "attestation", required = false) MultipartFile attestation,
            @RequestPart(value = "autre", required = false) MultipartFile autre,
            Authentication authentication,
            @RequestHeader("Authorization") String authHeader) {

        try {
            Long doctorantId = Long.parseLong(authentication.getName());
            log.info("Submitting dossier for doctorant: {}", doctorantId);

            // Map files to TypeDocument
            Map<TypeDocument, MultipartFile> files = new HashMap<>();
            if (diplome != null) files.put(TypeDocument.DIPLOME, diplome);
            if (cv != null) files.put(TypeDocument.CV, cv);
            if (lettreMotivation != null) files.put(TypeDocument.LETTRE_MOTIVATION, lettreMotivation);
            if (attestation != null) files.put(TypeDocument.ATTESTATION, attestation);
            if (autre != null) files.put(TypeDocument.AUTRE, autre);

            String token= authHeader.substring((7));
            DossierResponse response = dossierService.submitDossier(request, files, doctorantId, token);

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


    @GetMapping("/{doctorantId}/initial-date")
    public ResponseEntity<LocalDate>  getInitialInscriptionDate(@PathVariable Long doctorantId) {
        log.info("Getting inscription date for doctorant: {}", doctorantId);
        LocalDate date = dossierService.getInitialInscriptionDate(doctorantId);
        if (date == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(date);
    }
}
