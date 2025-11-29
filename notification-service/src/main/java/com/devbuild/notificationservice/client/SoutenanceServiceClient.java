package com.devbuild.notificationservice.client;

import com.devbuild.notificationservice.dto.DemandeInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "soutenance-service")
public interface SoutenanceServiceClient {

    @GetMapping("/api/soutenances/{demandeId}/status")
    DemandeInfo getDemandeInfo(@PathVariable("demandeId") Long demandeId);
}