package com.devbuild.soutenanceservice.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "juries")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Jury {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    @OneToMany(mappedBy = "jury", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<MembreJury> membres = new ArrayList<>();

    @Builder.Default
    private boolean rapportsFavorables = false;
}
