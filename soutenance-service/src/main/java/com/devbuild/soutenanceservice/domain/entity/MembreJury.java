package com.devbuild.soutenanceservice.domain.entity;

import com.devbuild.soutenanceservice.domain.enums.TypeMembreJury;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "membres_jury")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembreJury {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeMembreJury type;

    @ManyToOne
    @JoinColumn(name = "jury_id")
    private Jury jury;
}
