package com.devbuild.apigateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

        @Component
        public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

            @Value("${jwt.secret}")
            private String jwtSecret;

            private static final List<String> PUBLIC_ENDPOINTS = List.of(
                    "/api/auth/login",
                    "/api/users/register",
                    "/eureka",
                    "/actuator"
            );

            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
                ServerHttpRequest request = exchange.getRequest();
                String path = request.getURI().getPath();

                // Autoriser les endpoints publics
                if (isPublicEndpoint(path)) {
                    return chain.filter(exchange);
                }

                // Extraire et valider le token JWT
                String token = extractToken(request);

                if (token == null || !validateToken(token)) {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }

                // Extraire les informations utilisateur et les ajouter aux headers
                try {
                    Claims claims = extractClaims(token);
                    String userId = claims.getSubject();
                    String userRole = claims.get("role", String.class);

                    ServerHttpRequest modifiedRequest = request.mutate()
                            .header("X-User-Id", userId)
                            .header("X-User-Role", userRole)
                            .build();

                    return chain.filter(exchange.mutate().request(modifiedRequest).build());

                } catch (Exception e) {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }
            }

            private boolean isPublicEndpoint(String path) {
                return PUBLIC_ENDPOINTS.stream().anyMatch(path::startsWith);
            }

            private String extractToken(ServerHttpRequest request) {
                String bearerToken = request.getHeaders().getFirst("Authorization");
                if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                    return bearerToken.substring(7);
                }
                return null;
            }

            private SecretKey getSigningKey() {
                return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            }

            private boolean validateToken(String token) {
                try {
                    Jwts.parser()
                            .verifyWith(getSigningKey())
                            .build()
                            .parseSignedClaims(token);
                 return true;
                } catch (Exception e) {
                    return false;
                }
            }

            private Claims extractClaims(String token) {
                return Jwts.parser()
                        .verifyWith(getSigningKey())
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();
            }

            private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(status);
                response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

                String errorJson = String.format(
                        "{\"error\":\"%s\",\"status\":%d,\"path\":\"%s\"}",
                        message, status.value(), exchange.getRequest().getURI().getPath()
                );

                DataBuffer buffer = response.bufferFactory().wrap(errorJson.getBytes(StandardCharsets.UTF_8));
                return response.writeWith(Mono.just(buffer));
            }



            @Override
            public int getOrder() {
                return -100;
            }
        }