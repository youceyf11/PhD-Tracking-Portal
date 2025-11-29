package com.devbuild.inscriptionservice.client;

import com.devbuild.inscriptionservice.domain.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceClient {

    @Value("${services.user-service.url:http://user-service:8081}")
    private String userServiceUrl;

    private final RestClient restClient;

    /**
     * ✅ NOUVELLE VERSION : Utilise l'endpoint public /validate-role
     * Cet endpoint accepte les tokens de n'importe quel utilisateur authentifié
     */
    public UserResponse validateUserRole(Long userId, String expectedRole, String authToken) {
        String fullUrl = userServiceUrl + "/api/users/validate-role/" + userId + "?expectedRole=" + expectedRole;

        try {
            log.info("🔍 Validating user role via public endpoint: userId={}, expectedRole={}", userId, expectedRole);
            log.debug("🌐 Calling: {}", fullUrl);

            UserResponse user = restClient.get()
                    .uri(fullUrl)
                    .header("Authorization", "Bearer " + authToken)
                    .retrieve()
                    .body(UserResponse.class);

            if (user == null) {
                log.error("❌ User-service returned NULL for userId: {}", userId);
                return null;
            }

            log.info("✅ User {} validated successfully with role '{}'", userId, user.getRole());
            return user;

        } catch (RestClientResponseException e) {
            // Gérer spécifiquement les erreurs HTTP
            if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                log.warn("❌ User {} does NOT have role '{}'", userId, expectedRole);
                return null; // Role mismatch
            } else if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.warn("❌ User {} not found", userId);
                return null;
            } else {
                log.error("❌ HTTP {} error validating user role: {}",
                        e.getStatusCode().value(), e.getMessage());
                throw new RuntimeException("Erreur lors de la validation du rôle utilisateur: " + e.getMessage(), e);
            }
        } catch (RestClientException e) {
            log.error("❌ REST call failed to user-service");
            log.error("   URL: {}", fullUrl);
            log.error("   Error: {}", e.getMessage(), e);
            throw new RuntimeException("Impossible de contacter user-service: " + e.getMessage(), e);
        }
    }

    /**
     * Récupère un utilisateur par son ID (utilise aussi l'endpoint public)
     */
    public UserResponse getUserById(Long userId, String authToken) {
        String fullUrl = userServiceUrl + "/api/users/validate-role/" + userId;

        try {
            log.debug("🌐 Fetching user: {}", fullUrl);

            UserResponse user = restClient.get()
                    .uri(fullUrl)
                    .header("Authorization", "Bearer " + authToken)
                    .retrieve()
                    .body(UserResponse.class);

            if (user == null) {
                log.error("❌ User-service returned NULL for userId: {}", userId);
                return null;
            }

            log.debug("✅ User fetched: id={}, email={}, role='{}'",
                    user.getId(), user.getEmail(), user.getRole());
            return user;

        } catch (RestClientException e) {
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