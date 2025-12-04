package com.devbuild.soutenanceservice.domain.entity;

import com.devbuild.soutenanceservice.domain.enums.StatutDemande;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "demandes_soutenance")
public class DemandeSoutenance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long doctorantId;

    @Column(nullable = false)
    private String manuscritPath;

    @Builder.Default
    @OneToMany(mappedBy = "demandeSoutenance", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<DocumentSoutenance> documents = new ArrayList<>();


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutDemande statut = StatutDemande.EN_ATTENTE_PREREQUIS;

    @Column(nullable = false)
    private LocalDate dateSubmission;

    private LocalDate dateSoutenance;

    private LocalTime heureSoutenance;

    private String lieuSoutenance;

    @Builder.Default
    private boolean derrogationDuree = false;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "prerequis_id")
    @ToString.Exclude
    private Prerequis prerequis;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "jury_id")
    @ToString.Exclude
    private Jury jury;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
