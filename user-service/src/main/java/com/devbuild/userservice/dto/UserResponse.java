package com.devbuild.userservice.dto;


import com.devbuild.userservice.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.devbuild.userservice.enums.Role;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;
    private String email;
    private String nom;
    private String prenom;
    private Role role;
    private LocalDate dateCreation;
    private Boolean active;
    private LocalDateTime lastLogin;


    public static UserResponse fromEntity(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nom(user.getNom())
                .prenom(user.getPrenom())
                .role(user.getRole())
                .dateCreation(user.getDateCreation())
                .active(user.getActive())
                .lastLogin(user.getLastLogin())
                .build();
    }

}
