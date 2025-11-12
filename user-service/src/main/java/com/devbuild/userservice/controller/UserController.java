package com.devbuild.userservice.controller;

import com.devbuild.userservice.dto.UpdateProfileRequest;
import com.devbuild.userservice.dto.UserRequest;
import com.devbuild.userservice.dto.UserResponse;
import com.devbuild.userservice.entity.User;
import com.devbuild.userservice.enums.Role;
import com.devbuild.userservice.exception.ResourceNotFoundException;
import com.devbuild.userservice.exception.UserNotFoundException;
import com.devbuild.userservice.repository.UserRepository;
import com.devbuild.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody UserRequest request) {
        User user= User.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .role(request.getRole() != null ? request.getRole() : Role.DOCTORANT)
                .build();
        User createdUser = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(UserResponse.fromEntity(createdUser));
    }


    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN' , 'DIRECTEUR' )")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users= userService.getAllUsers().stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(users);
    }


    @GetMapping("/{email}")
    @PreAuthorize("hasAnyRole('ADMIN' , 'DIRECTEUR' )")
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String email) {
        User user = userService.getUserEmail(email);
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }

    @GetMapping("/id/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setNom(user.getNom());
        response.setPrenom(user.getPrenom());
        response.setRole(user.getRole());

        return ResponseEntity.ok(response);
    }


    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateUserRole(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        Role newRole = Role.valueOf(payload.get("role"));
        User updatedUser = userService.updateUserRole(id, newRole);
        return ResponseEntity.ok(UserResponse.fromEntity(updatedUser));
    }

    @PutMapping("/{id}/desactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> desactivateUser(@PathVariable Long id) {
        userService.desactivateUser(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/current")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> updateOwnProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {

        String email = authentication.getName();
        User currentUser = userService.getUserEmail(email);

        currentUser.setNom(request.getNom());
        currentUser.setPrenom(request.getPrenom());

        User updatedUser = userService.updateUser(currentUser);
        return ResponseEntity.ok(UserResponse.fromEntity(updatedUser));
    }

    // Ajouter cet endpoint dans votre UserController (user-service)

    /**
     * Endpoint public pour valider qu'un utilisateur a un rôle spécifique
     * Utilisé par les autres microservices (inscription-service, etc.)
     * Accessible à tous les utilisateurs authentifiés
     */
    @GetMapping("/validate-role/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> validateUserRole(
            @PathVariable Long userId,
            @RequestParam(required = false) String expectedRole) {

        log.info("Validating user role: userId={}, expectedRole={}", userId, expectedRole);

        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé: " + userId));

            // Créer la réponse avec les infos nécessaires
            UserResponse response = UserResponse.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .nom(user.getNom())
                    .prenom(user.getPrenom())
                    .role(user.getRole())
                    .build();

            // Si un rôle est spécifié, vérifier la correspondance
            if (expectedRole != null && !expectedRole.isEmpty()) {
                String normalizedUserRole = normalizeRole(user.getRole());
                String normalizedExpectedRole = normalizeRole(expectedRole);

                if (!normalizedUserRole.equals(normalizedExpectedRole)) {
                    log.warn("Role mismatch for user {}: has '{}', expected '{}'",
                            userId, user.getRole(), expectedRole);
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(response); // Retourne quand même les infos mais avec 403
                }
            }

            log.info("User {} validated successfully with role '{}'", userId, user.getRole());
            return ResponseEntity.ok(response);

        } catch (ResourceNotFoundException e) {
            log.error("User not found: {}", userId);
            throw e;
        }
    }

    /**
     * Normalise un rôle (retire ROLE_ et met en majuscules)
     */
    private String normalizeRole(Role role) {
        if (role == null) return "";
        String roleName = role.name().trim().toUpperCase();
        return roleName.startsWith("ROLE_") ? roleName.substring(5) : roleName;
    }

    private String normalizeRole(String role) {
        if (role == null) return "";
        String normalized = role.trim().toUpperCase();
        return normalized.startsWith("ROLE_") ? normalized.substring(5) : normalized;
    }

}
