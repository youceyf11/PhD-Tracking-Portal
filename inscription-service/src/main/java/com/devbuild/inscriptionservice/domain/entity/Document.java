package com.devbuild.inscriptionservice.domain.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import com.devbuild.inscriptionservice.domain.enums.TypeDocument;

import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String nomFichier;

    @Column(nullable = false, length = 500)
    private String cheminFichier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TypeDocument typeDocument;

    @Column(nullable = false, length = 50)
    private String mimeType;

    @Column(nullable = false)
    private Long tailleFichier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dossier_id", nullable = false)
    private DossierInscription dossier;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime uploadedAt;


    public String getExtension() {
        int lastDot = nomFichier.lastIndexOf('.');
        return lastDot > 0 ? nomFichier.substring(lastDot + 1).toLowerCase() : "";
    }

    public double getTailleEnMo() {
        return tailleFichier / (1024.0 * 1024.0);
    }
}
