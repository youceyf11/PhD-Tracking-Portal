package com.devbuild.userservice.controller;

import com.devbuild.userservice.dto.LoginRequest;
import com.devbuild.userservice.dto.UserRequest;
import com.devbuild.userservice.dto.UserResponse;
import com.devbuild.userservice.entity.User;
import com.devbuild.userservice.service.JwtService;
import com.devbuild.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;

    /**
     * Login endpoint - Returns JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            User user = userService.getUserEmail(request.getEmail());

            // Check if user is active
            if (!user.isEnabled()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("User account is deactivated");
            }

            userService.updateLastLogin(user.getEmail());
            User userComplet = userService.getUserEmail(user.getEmail());


            String token = jwtService.generateToken(user);

            UserResponse response = UserResponse.builder()
                    .id(userComplet.getId())
                    .token(token)
                    .email(userComplet.getEmail())
                    .nom(userComplet.getNom())
                    .prenom(userComplet.getPrenom())
                    .role(userComplet.getRole())
                    .dateCreation(userComplet.getDateCreation())
                    .active(userComplet.getActive())
                    .lastLogin(userComplet.getLastLogin())
                    .build();

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid email or password");
        }
    }


    /*@PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody LoginRequest request) {
        User user = User.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .role(request.getRole() != null ? request.getRole() : Role.DOCTORANT)
                .build();

        User createdUser = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(LoginResponse.fromEntity(createdUser));
    }*/
}