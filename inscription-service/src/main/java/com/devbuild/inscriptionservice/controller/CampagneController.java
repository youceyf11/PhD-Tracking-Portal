package com.devbuild.inscriptionservice.controller;

import com.devbuild.inscriptionservice.domain.dto.request.CampagneRequest;
import com.devbuild.inscriptionservice.domain.dto.response.ApiResponse;
import com.devbuild.inscriptionservice.domain.dto.response.CampagneResponse;
import com.devbuild.inscriptionservice.service.CampagneService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/campagnes")
@RequiredArgsConstructor
@Slf4j
public class CampagneController {

    private final CampagneService campagneService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CampagneResponse>> createCampagne(
            @Valid @RequestBody CampagneRequest request) {
        log.info("Creating new campagne: {}", request.getNom());
        CampagneResponse response = campagneService.createCampagne(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Campagne créée avec succès", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CampagneResponse>> updateCampagne(
            @PathVariable Long id,
            @Valid @RequestBody CampagneRequest request) {
        log.info("Updating campagne: id={}", id);
        CampagneResponse response = campagneService.updateCampagne(id, request);
        return ResponseEntity.ok(ApiResponse.success("Campagne mise à jour avec succès", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR', 'DOCTORANT')")
    public ResponseEntity<ApiResponse<CampagneResponse>> getCampagneById(@PathVariable Long id) {
        log.info("Getting campagne by id: {}", id);
        CampagneResponse response = campagneService.getCampagneById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR', 'DOCTORANT')")
    public ResponseEntity<ApiResponse<List<CampagneResponse>>> getAllCampagnes() {
        log.info("Getting all campagnes");
        List<CampagneResponse> responses = campagneService.getAllCampagnes();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR', 'DOCTORANT')")
    public ResponseEntity<ApiResponse<List<CampagneResponse>>> getActiveCampagnes() {
        log.info("Getting active campagnes");
        List<CampagneResponse> responses = campagneService.getActiveCampagnes();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/current")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR', 'DOCTORANT')")
    public ResponseEntity<ApiResponse<CampagneResponse>> getCurrentActiveCampagne() {
        log.info("Getting current active campagne");
        CampagneResponse response = campagneService.getCurrentActiveCampagne();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCampagne(@PathVariable Long id) {
        log.info("Deleting campagne: id={}", id);
        campagneService.deleteCampagne(id);
        return ResponseEntity.ok(ApiResponse.success("Campagne supprimée avec succès", null));
    }
}