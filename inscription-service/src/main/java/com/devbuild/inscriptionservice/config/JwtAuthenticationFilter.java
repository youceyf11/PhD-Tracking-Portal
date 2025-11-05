package com.devbuild.inscriptionservice.config;

import org.springframework.web.filter.OncePerRequestFilter;

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

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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

                // Valider le token avec la même clé que user-service
                SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

                Claims claims = Jwts.parser()
                        .verifyWith(key)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

                // Extraire userId (maintenant String dans le JWT)
                String userId = claims.get("userId", String.class);

                // Extraire email
                String email = claims.getSubject();

                // Extraire roles (maintenant List<String> dans le JWT)
                @SuppressWarnings("unchecked")
                List<String> roles = claims.get("roles", List.class);

                if (userId != null && roles != null && !roles.isEmpty()) {
                    // Convertir les rôles en authorities Spring Security
                    List<SimpleGrantedAuthority> authorities = roles.stream()
                            .map(role -> {
                                // Ajouter "ROLE_" si pas déjà présent
                                String roleStr = role.startsWith("ROLE_") ? role : "ROLE_" + role;
                                return new SimpleGrantedAuthority(roleStr);
                            })
                            .collect(Collectors.toList());

                    // Créer l'authentication avec userId comme principal
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userId, null, authorities);

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Définir l'authentification dans le SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    log.debug("JWT validated successfully - User: {} | Email: {} | Roles: {}",
                            userId, email, roles);
                } else {
                    log.warn("JWT validation failed - Missing required claims: userId={}, roles={}",
                            userId, roles);
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
        // Ne pas filtrer les endpoints publics
        String path = request.getRequestURI();
        return path.startsWith("/actuator/health") ||
                path.startsWith("/actuator/info") ||
                path.equals("/error");
    }
}
