package com.devbuild.inscriptionservice.domain.entity;

import com.devbuild.inscriptionservice.domain.enums.StatutDossier;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "dossiers_inscription", indexes = {
        @Index(name = "idx_doctorant_id", columnList = "doctorantId"),
        @Index(name = "idx_directeur_id", columnList = "directeurId"),
        @Index(name = "idx_statut", columnList = "statut")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DossierInscription {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long doctorantId; // From User Service

    @Column(nullable = false, length = 1000)
    private String sujetThese;

    @Column(nullable = false)
    private Long directeurId; // From User Service

    @Column(length = 500)
    private String collaboration;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private StatutDossier statut = StatutDossier.EN_ATTENTE_DIRECTEUR;

    @Column(nullable = false)
    private LocalDateTime dateSubmission;

    @Column(nullable = false)
    private LocalDateTime initialInscriptionDate;

    @Column(nullable = false)
    @Builder.Default
    private Boolean derogationFlag = false;

    @Column(length = 1000)
    private String commentaireDirecteur;

    @Column(length = 1000)
    private String commentaireAdmin;

    private LocalDateTime dateValidationDirecteur;
    private LocalDateTime dateValidationAdmin;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isReenrollment = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campagne_id", nullable = false)
    private Campagne campagne;

    @OneToMany(mappedBy = "dossier", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Document> documents = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // Returns true if more than 3 years have passed since the initial registration date.
    public boolean isDepasseDureeInitiale() {
        return ChronoUnit.YEARS.between(initialInscriptionDate, LocalDateTime.now()) > 3;
    }

    // Returns true if 5 years or more have passed since the initial registration date.
    public boolean approcheLimiteMaximale() {
        long years = ChronoUnit.YEARS.between(initialInscriptionDate, LocalDateTime.now());
        return years >= 5;
    }

    // Returns true if more than 6 years have passed since the initial registration date.
    public boolean depasseLimiteMaximale() {
        long years = ChronoUnit.YEARS.between(initialInscriptionDate, LocalDateTime.now());
        return years > 6;
    }

    //Returns the number of years since the initial registration date.
    public long getAnneesDepuisInscription() {
        return ChronoUnit.YEARS.between(initialInscriptionDate, LocalDateTime.now());
    }

    // Returns true if the file status allows validation (waiting for director or admin).
    public boolean peutEtreValide() {
        return statut == StatutDossier.EN_ATTENTE_DIRECTEUR ||
                statut == StatutDossier.EN_ATTENTE_ADMIN;
    }

    // Adds a document to the list and sets this file as the parent of the document.
    public void addDocument(Document document) {
        documents.add(document);
        document.setDossier(this);
    }

    // Removes a document from the list and removes the reference to the file in the document.
    public void removeDocument(Document document) {
        documents.remove(document);
        document.setDossier(null);
    }
}
