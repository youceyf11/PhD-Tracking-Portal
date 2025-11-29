package com.devbuild.soutenanceservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@FeignClient(name = "user-service")
public interface UserServiceClient {

    @GetMapping("/api/users/{id}/role")
    String getUserRole(@PathVariable Long id);
}
