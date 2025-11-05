package com.devbuild.userservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    @PostConstruct
    public void init() {
        if (jwtSecret == null || jwtSecret.isEmpty()) {
            throw new IllegalStateException("jwt.secret must be configured!");
        }
        logger.info("JWT Authentication Filter initialized successfully");
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator/health") ||
                path.startsWith("/actuator/info") ||
                path.startsWith("/eureka") ||
                path.equals("/api/users/register") ||
                path.equals("/api/auth/login") ||
                path.equals("/api/auth/register");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authHeader.substring(7);
            SecretKey key = new SecretKeySpec(jwtSecret.getBytes(), "HmacSHA512");

            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            String email = claims.getSubject();

            List<String> roles = extractRoles(claims);

            if (email != null && !roles.isEmpty() &&
                    SecurityContextHolder.getContext().getAuthentication() == null) {

                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(role -> {
                            String normalizedRole = role.toUpperCase().trim();
                            // Ajouter ROLE_ si absent
                            if (!normalizedRole.startsWith("ROLE_")) {
                                normalizedRole = "ROLE_" + normalizedRole;
                            }
                            return new SimpleGrantedAuthority(normalizedRole);
                        })
                        .collect(Collectors.toList());

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null,
                                authorities
                        );

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                logger.info("JWT validated - User: {} | Authorities: {}", email, authorities);
            }

        } catch (Exception e) {
            logger.error("JWT validation failed: {}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extrait les rôles du JWT (gère String, List, ou absent)
     */
    private List<String> extractRoles(Claims claims) {
        List<String> roles = new ArrayList<>();

        // 1. Essayer "roles" (liste d'objets)
        Object rolesObj = claims.get("roles");
        if (rolesObj instanceof List) {
            ((List<?>) rolesObj).forEach(role -> {
                if (role instanceof String) {
                    roles.add((String) role);
                } else if (role != null) {
                    roles.add(role.toString());
                }
            });
            if (!roles.isEmpty()) return roles;
        }

        // 2. Essayer "roles" (string unique ou séparée par virgules)
        if (rolesObj instanceof String) {
            String rolesStr = (String) rolesObj;
            if (rolesStr.contains(",")) {
                roles.addAll(List.of(rolesStr.split(",")));
            } else {
                roles.add(rolesStr);
            }
            if (!roles.isEmpty()) return roles;
        }

        // 3. Essayer "authorities" (format Spring Security)
        Object authObj = claims.get("authorities");
        if (authObj instanceof List) {
            ((List<?>) authObj).forEach(auth -> {
                if (auth instanceof String) {
                    roles.add((String) auth);
                } else if (auth != null) {
                    roles.add(auth.toString());
                }
            });
        }

        // 4. Fallback: rôle par défaut si aucun trouvé
        if (roles.isEmpty()) {
            logger.warn("Aucun rôle trouvé dans le JWT pour user: {}", claims.getSubject());
            roles.add("USER"); // Rôle par défaut
        }

        return roles;
    }

}