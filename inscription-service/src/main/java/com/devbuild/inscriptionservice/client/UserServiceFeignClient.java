package com.devbuild.inscriptionservice.client;

import com.devbuild.inscriptionservice.config.FeignClientConfig;
import com.devbuild.inscriptionservice.domain.dto.response.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service", configuration = FeignClientConfig.class)
public interface UserServiceFeignClient {

    @GetMapping("/api/users/validate-role/{userId}")
    UserResponse validateUserRole(@PathVariable("userId") Long userId,
                                  @RequestParam("expectedRole") String expectedRole);

    @GetMapping("/api/users/id/{userId}") 
    UserResponse getUserById(@PathVariable("userId") Long userId);
}