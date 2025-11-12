package com.devbuild.inscriptionservice.domain.dto.response;

import lombok.Data;


@Data
public class UserResponse {
    private Long id;
    private String role;
    private String email;
    private String nom;
    private String prenom;
}
