package com.devbuild.inscriptionservice.client;

import com.devbuild.inscriptionservice.domain.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceClient {

    private final UserServiceFeignClient userServiceFeignClient;

    // Removed 'authToken' parameter
    public UserResponse validateUserRole(Long userId, String expectedRole) {
        try {
            log.info("🔍 Validating user role via Feign: userId={}, expectedRole={}", userId, expectedRole);
            return userServiceFeignClient.validateUserRole(userId, expectedRole);
        } catch (Exception e) {
            log.error("Call to User Service failed", e);
            throw new RuntimeException("Service Utilisateur indisponible: " + e.getMessage());
        }
    }

    // Removed 'authToken' parameter
    public UserResponse getUserById(Long userId) {
        try {
            log.debug("🌐 Fetching user via Feign: {}", userId);
            return userServiceFeignClient.getUserById(userId);
        } catch (Exception e) {
            // We return null here so we don't crash the whole response if just the name is missing
            log.error("❌ Error fetching user {}: {}", userId, e.getMessage());
            return null;
        }
    }

    public String getUserEmail(Long userId) {
        try {
            UserResponse user = getUserById(userId);
            return user != null ? user.getEmail() : null;
        } catch (Exception e) {
            return null;
        }
    }

    public String getUserFullName(Long userId) {
        try {
            UserResponse user = getUserById(userId);
            if (user == null) return null;
            return (user.getPrenom() != null ? user.getPrenom() : "") + " " +
                    (user.getNom() != null ? user.getNom() : "");
        } catch (Exception e) {
            return null;
        }
    }
}