package com.devbuild.userservice.service;

        import com.devbuild.userservice.dto.LoginRequest;
        import com.devbuild.userservice.dto.LoginResponse;
        import com.devbuild.userservice.entity.User;
        import com.devbuild.userservice.repository.UserRepository;
        import lombok.RequiredArgsConstructor;
        import org.slf4j.Logger;
        import org.slf4j.LoggerFactory;
        import org.springframework.security.crypto.password.PasswordEncoder;
        import org.springframework.stereotype.Service;


        @Service
        @RequiredArgsConstructor
        public class AuthService {

            private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

            private final UserRepository userRepository;
            private final JwtService jwtService;
            private final PasswordEncoder passwordEncoder;

            public LoginResponse login(LoginRequest request) {
                User user = userRepository.findByEmail(request.getEmail())
                        .orElseThrow(() -> new RuntimeException("Invalid credentials"));

                if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                    throw new RuntimeException("Invalid credentials");
                }

                String token = jwtService.generateToken(user);

                // Extraction du rôle unique
                String roleName = "ROLE_" + user.getRole().name();

                // LOG pour vérifier la génération
                logger.info("Token generated for user: {} with role: {}", user.getEmail(), roleName);

                return LoginResponse.builder()
                        .token(token)
                        .email(user.getEmail())
                        .build();
            }
        }