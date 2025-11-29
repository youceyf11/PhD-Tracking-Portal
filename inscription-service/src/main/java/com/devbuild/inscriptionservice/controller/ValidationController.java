package com.devbuild.inscriptionservice.controller;


import com.devbuild.inscriptionservice.domain.dto.request.ValidationAdminRequest;
import com.devbuild.inscriptionservice.domain.dto.request.ValidationDirecteurRequest;
import com.devbuild.inscriptionservice.domain.dto.response.ApiResponse;
import com.devbuild.inscriptionservice.domain.dto.response.DossierResponse;
import com.devbuild.inscriptionservice.service.ValidationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/validations")
@RequiredArgsConstructor
@Slf4j
public class ValidationController {

    private final ValidationService validationService;

    @PostMapping("/directeur")
    @PreAuthorize("hasRole('DIRECTEUR')")
    public ResponseEntity<ApiResponse<DossierResponse>> validateByDirecteur(
            @Valid @RequestBody ValidationDirecteurRequest request,
            Authentication authentication) {

        Long directeurId = Long.parseLong(authentication.getName());
        log.info("Directeur {} validating dossier: {}, approved: {}",
                directeurId, request.getDossierId(), request.getApprouve());

        DossierResponse response = validationService.validateByDirecteur(
                request.getDossierId(),
                directeurId,
                request.getApprouve(),
                request.getCommentaire()
        );

        String message = request.getApprouve()
                ? "Dossier approuvé par le directeur avec succès"
                : "Dossier rejeté par le directeur";

        return ResponseEntity.ok(ApiResponse.success(message, response));
    }

    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DossierResponse>> validateByAdmin(
            @Valid @RequestBody ValidationAdminRequest request,
            Authentication authentication) {

        log.info("Admin validating dossier: {}, approved: {}",
                request.getDossierId(), request.getApprouve());

        DossierResponse response = validationService.validateByAdmin(
                request.getDossierId(),
                request.getApprouve(),
                request.getCommentaire(),
                request.getAccorderDerogation()
        );

        String message = request.getApprouve()
                ? "Dossier validé par l'administration avec succès"
                : "Dossier rejeté par l'administration";

        return ResponseEntity.ok(ApiResponse.success(message, response));
    }
}
