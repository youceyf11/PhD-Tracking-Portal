package com.devbuild.soutenanceservice.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rapports")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Rapport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String path;

    private boolean favorable;

    @ManyToOne
    @JoinColumn(name = "demande_id")
    private DemandeSoutenance demandeSoutenance;
}
