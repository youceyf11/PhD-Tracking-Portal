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

    @OneToMany(mappedBy = "jury", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MembreJury> membres = new ArrayList<>();

    private boolean rapportsFavorables = false;
}
