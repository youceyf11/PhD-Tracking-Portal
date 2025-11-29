package com.devbuild.notificationservice.client;

import com.devbuild.notificationservice.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@FeignClient(name = "user-service")
public interface UserServiceClient {

    @GetMapping("/api/users/validate-role/{userId}")
    UserResponse getUserById(@PathVariable("userId") Long userId);
}