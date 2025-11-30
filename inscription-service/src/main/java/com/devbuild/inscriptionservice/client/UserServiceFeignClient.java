package com.devbuild.inscriptionservice.client;

import com.devbuild.inscriptionservice.config.FeignClientConfig;
import com.devbuild.inscriptionservice.domain.dto.response.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service", url = "${services.user-service.url:http://user-service:8081}", configuration = FeignClientConfig.class)
public interface UserServiceFeignClient {

    @GetMapping("/api/users/validate-role/{userId}")
    UserResponse validateUserRole(@PathVariable Long userId, @RequestParam String expectedRole);

    @GetMapping("/api/users/{userId}")
    UserResponse getUserById(@PathVariable Long userId);
}
