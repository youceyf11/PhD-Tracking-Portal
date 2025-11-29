package com.devbuild.inscriptionservice.repository;

import com.devbuild.inscriptionservice.domain.entity.Document;
import com.devbuild.inscriptionservice.domain.enums.TypeDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByDossierId(Long dossierId);

    List<Document> findByDossierIdAndTypeDocument(Long dossierId, TypeDocument typeDocument);

    void deleteByDossierId(Long dossierId);
}
