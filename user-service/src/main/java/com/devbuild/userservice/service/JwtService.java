package com.devbuild.userservice.service;

import com.devbuild.userservice.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}")  // 24 heures par défaut
    private long jwtExpiration;

    @Value("${jwt.refresh.expiration:604800000}")  // 7 jours par défaut
    private long refreshExpiration;

    /**
     * Génère un access token pour l'utilisateur
     */
    /**
     * Génère un access token pour l'utilisateur
     */
    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();

        claims.put("userId", String.valueOf(user.getId()));

        String roleWithPrefix = "ROLE_" + user.getRole().name();
        claims.put("roles", List.of(roleWithPrefix));
        claims.put("email", user.getEmail());
        claims.put("nom", user.getNom());
        claims.put("prenom", user.getPrenom());

        return createToken(claims, user.getEmail(), jwtExpiration);
    }



    /**
     * Génère un refresh token
     */
    public String generateRefreshToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", String.valueOf(user.getId()));
        claims.put("type", "refresh");

        return createToken(claims, user.getEmail(), refreshExpiration);
    }

    /**
     * Crée le token JWT
     */
    private String createToken(Map<String, Object> claims, String subject, long expiration) {
        SecretKey key = new SecretKeySpec(jwtSecret.getBytes(), "HmacSHA512");

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
    }

    /**
     * Extrait l'email du token
     */
    public String extractEmail(String token) {
        SecretKey key = new SecretKeySpec(jwtSecret.getBytes(), "HmacSHA512");
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * Extrait le userId du token
     */
    public String extractUserId(String token) {
        SecretKey key = new SecretKeySpec(jwtSecret.getBytes(), "HmacSHA512");
        Object userIdObj = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("userId");

        return userIdObj != null ? String.valueOf(userIdObj) : null;
    }

    /**
     * Valide le token
     */
    public boolean isTokenValid(String token) {
        try {
            SecretKey key = new SecretKeySpec(jwtSecret.getBytes(), "HmacSHA512");
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Vérifie si le token est expiré
     */
    public boolean isTokenExpired(String token) {
        try {
            SecretKey key = new SecretKeySpec(jwtSecret.getBytes(), "HmacSHA512");
            Date expiration = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getExpiration();

            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

}
