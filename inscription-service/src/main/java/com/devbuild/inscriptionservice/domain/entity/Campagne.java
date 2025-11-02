package com.devbuild.inscriptionservice.domain.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import jakarta.persistence.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/*
    * Représente les campagnes d'inscription gérées par l'admin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "campagnes")
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Campagne {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nom;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private LocalDateTime dateOuverture;

    @Column(nullable = false)
    private LocalDateTime dateFermeture;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = false;

    @OneToMany(mappedBy = "campagne", cascade = CascadeType.ALL)
    @Builder.Default
    private List<DossierInscription> dossiers = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    /*
    Vérifie si la campagne est active, que la date actuelle est après la date d’ouverture
     et avant la date de fermeture.
    Retourne true si la campagne est en cours.
     */
    public boolean isInProgress() {
        LocalDateTime now = LocalDateTime.now();
        return active &&
                now.isAfter(dateOuverture) &&
                now.isBefore(dateFermeture);
    }

    /*
    Retourne true si la date actuelle est après la date de fermeture,
    donc si la campagne est terminée.
     */
    public boolean isClosed() {
        return LocalDateTime.now().isAfter(dateFermeture);
    }


    /*
    Retourne true si la date actuelle est avant la date d’ouverture,
     donc si la campagne n’a pas encore commencé.
     */
    public boolean isNotStarted() {
        return LocalDateTime.now().isBefore(dateOuverture);
    }

}
