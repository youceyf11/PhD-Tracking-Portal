package com.devbuild.soutenanceservice.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            String authHeader = request.getHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

                Claims claims = Jwts.parser()
                        .verifyWith(key)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

                // 1. Extract UserId safely (Handle Integer vs String)
                String userId = null;
                Object userIdClaim = claims.get("userId");
                if (userIdClaim != null) {
                    userId = String.valueOf(userIdClaim);
                }

                String email = claims.getSubject();

                // 2. Robust Role Extraction (The Fix)
                List<String> roles = null;

                // Attempt A: Try standard "roles" (List)
                try {
                    roles = claims.get("roles", List.class);
                } catch (Exception e) {
                    // Ignore, try next method
                }

                // Attempt B: Try singular "role" (String) -> Fix for your specific token
                if (roles == null) {
                    try {
                        String singleRole = claims.get("role", String.class);
                        if (singleRole != null) {
                            roles = Collections.singletonList(singleRole);
                        }
                    } catch (Exception e) {
                        // Ignore
                    }
                }

                // Attempt C: Try "authorities" (Common in Spring Security)
                if (roles == null) {
                    try {
                        roles = claims.get("authorities", List.class);
                    } catch (Exception e) {
                        // Ignore
                    }
                }

                // 3. Create Authentication
                if (userId != null && roles != null && !roles.isEmpty()) {
                    // Convert roles to Spring Security authorities
                    List<SimpleGrantedAuthority> authorities = roles.stream()
                            .map(role -> {
                                // Add "ROLE_" prefix if missing
                                String roleStr = role.startsWith("ROLE_") ? role : "ROLE_" + role;
                                return new SimpleGrantedAuthority(roleStr);
                            })
                            .collect(Collectors.toList());

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userId, null, authorities);

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    log.debug("JWT validated successfully - User: {} | Roles: {}", userId, roles);
                } else {
                    // Log ALL claims to help debug if it fails again
                    log.warn("JWT validation failed - userId={}, roles={}. Full Claims: {}",
                            userId, roles, claims);
                }
            }
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (io.jsonwebtoken.security.SignatureException e) {
            log.error("JWT signature validation failed: {}", e.getMessage());
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            log.error("JWT token is malformed: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Cannot validate JWT token: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator/health") ||
                path.startsWith("/actuator/info") ||
                path.equals("/error");
    }
}