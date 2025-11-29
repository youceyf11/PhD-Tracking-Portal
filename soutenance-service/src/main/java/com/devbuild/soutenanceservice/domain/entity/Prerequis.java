package com.devbuild.soutenanceservice.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "prerequis")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Prerequis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int nbArticlesQ1Q2;

    private int nbConferences;

    private int heuresFormation;

    private boolean valide = false;
}
