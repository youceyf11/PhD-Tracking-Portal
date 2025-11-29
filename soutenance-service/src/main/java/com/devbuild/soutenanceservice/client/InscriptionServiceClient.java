package com.devbuild.soutenanceservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDate;

@FeignClient(name = "inscription-service")
public interface InscriptionServiceClient {

    @GetMapping("/api/dossiers/{doctorantId}/initial-date")
    LocalDate getInitialInscriptionDate(@PathVariable Long doctorantId);
}
