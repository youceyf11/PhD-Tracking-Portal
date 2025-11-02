package com.devbuild.inscriptionservice.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

/**
 * Client pour communiquer avec le user-service
 * Utilisé pour récupérer des informations sur les utilisateurs (doctorant, directeur)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceClient {

    @Value("${services.user-service.url:http://user-service:8081}")
    private String userServiceUrl;

    private final RestClient restClient;

    /**
     * Récupère les informations d'un utilisateur par son ID
     * @param userId ID de l'utilisateur
     * @param authToken Token JWT pour l'authentification
     * @return Map contenant les infos utilisateur
     */
    public Map<String, Object> getUserById(Long userId, String authToken) {
        try {
            log.debug("Fetching user info for userId: {}", userId);

            return restClient.get()
                    .uri(userServiceUrl + "/api/users/" + userId)
                    .header("Authorization", "Bearer " + authToken)
                    .retrieve()
                    .body(Map.class);

        } catch (RestClientException e) {
            log.error("Error fetching user info for userId: {}", userId, e);
            return null;
        }
    }

    /**
     * Vérifie si un utilisateur existe et a un rôle spécifique
     * @param userId ID de l'utilisateur
     * @param role Rôle attendu (DOCTORANT, DIRECTEUR, etc.)
     * @param authToken Token JWT
     * @return true si l'utilisateur existe avec ce rôle
     */
    public boolean validateUserRole(Long userId, String role, String authToken) {
        try {
            Map<String, Object> user = getUserById(userId, authToken);
            if (user == null) {
                return false;
            }

            Object roles = user.get("roles");
            if (roles instanceof java.util.List) {
                return ((java.util.List<?>) roles).contains(role);
            }

            return false;
        } catch (Exception e) {
            log.error("Error validating user role for userId: {}", userId, e);
            return false;
        }
    }

    /**
     * Récupère l'email d'un utilisateur
     * @param userId ID de l'utilisateur
     * @param authToken Token JWT
     * @return Email de l'utilisateur ou null
     */
    public String getUserEmail(Long userId, String authToken) {
        try {
            Map<String, Object> user = getUserById(userId, authToken);
            return user != null ? (String) user.get("email") : null;
        } catch (Exception e) {
            log.error("Error fetching user email for userId: {}", userId, e);
            return null;
        }
    }

    /**
     * Récupère le nom complet d'un utilisateur
     * @param userId ID de l'utilisateur
     * @param authToken Token JWT
     * @return Nom complet ou null
     */
    public String getUserFullName(Long userId, String authToken) {
        try {
            Map<String, Object> user = getUserById(userId, authToken);
            if (user == null) {
                return null;
            }

            String firstName = (String) user.get("firstName");
            String lastName = (String) user.get("lastName");

            return (firstName != null && lastName != null)
                    ? firstName + " " + lastName
                    : null;

        } catch (Exception e) {
            log.error("Error fetching user full name for userId: {}", userId, e);
            return null;
        }
    }
}