package com.devbuild.userservice.dto;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
    private String token;
    private String email;
    private String role;
    private String nom;
    private String prenom;
}