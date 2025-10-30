package com.devbuild.userservice.entity;


import com.devbuild.userservice.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Column(nullable = false)
    private String password;


    @NotBlank(message = "Le nom est obligatoire")
    @Column(nullable = false, length = 50)
    private String nom;


    @NotBlank(message = "Le prénom est obligatoire")
    @Column(nullable = false, length = 50)
    private String prenom;


    @NotNull(message = "Le rôle est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Role role = Role.DOCTORANT;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDate dateCreation;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column
    private LocalDateTime lastLogin;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return active;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
