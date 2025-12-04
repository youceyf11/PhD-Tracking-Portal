package com.devbuild.soutenanceservice.domain.entity;

import com.devbuild.soutenanceservice.domain.enums.TypeDocumentSoutenance;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "documents_soutenance")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentSoutenance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String path;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeDocumentSoutenance type;

    @ManyToOne
    @JoinColumn(name = "demande_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private DemandeSoutenance demandeSoutenance;
}
