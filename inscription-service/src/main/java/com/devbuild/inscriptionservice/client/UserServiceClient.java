package com.devbuild.inscriptionservice.client;

import com.devbuild.inscriptionservice.domain.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceClient {


    private final UserServiceFeignClient userServiceFeignClient;

    public UserResponse validateUserRole(Long userId, String expectedRole, String authToken) {
        // Note: Auth token is handled by the interceptor; no need to pass it explicitly
        try {
            log.info("🔍 Validating user role via Feign: userId={}, expectedRole={}", userId, expectedRole);
            return userServiceFeignClient.validateUserRole(userId, expectedRole);
        } catch (Exception e) {
            // Handle exceptions as before (e.g., 404, 403)
            log.error("Call to User Service failed", e);
            throw new RuntimeException("Service Utilisateur indisponible");
        }
    }

    public UserResponse getUserById(Long userId, String authToken) {
        // Note: Auth token is handled by the interceptor
        try {
            log.debug("🌐 Fetching user via Feign: {}", userId);
            return userServiceFeignClient.getUserById(userId);
        } catch (Exception e) {
            log.error("❌ Error fetching user {}: {}", userId, e.getMessage());
            return null;
        }
    }

    public String getUserEmail(Long userId, String authToken) {
        try {
            UserResponse user = getUserById(userId, authToken);
            return user != null ? user.getEmail() : null;
        } catch (Exception e) {
            log.error("Error fetching user email for userId: {}", userId, e);
            return null;
        }
    }

    public String getUserFullName(Long userId, String authToken) {
        try {
            UserResponse user = getUserById(userId, authToken);

            if (user == null) {
                return null;
            }

            return (user.getPrenom() != null && user.getNom() != null)
                    ? user.getPrenom() + " " + user.getNom()
                    : null;

        } catch (Exception e) {
            log.error("Error fetching user full name for userId: {}", userId, e);
            return null;
        }
    }
}