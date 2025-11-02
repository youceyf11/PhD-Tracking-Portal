package com.devbuild.inscriptionservice.domain.enums;

public enum TypeDocument {

    DIPLOME("Diplôme"),
    CV("Curriculum Vitae"),
    LETTRE_MOTIVATION("Lettre de motivation"),
    ATTESTATION("Attestation"),
    AUTRE("Autre document");

    private final String description;

    TypeDocument(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

}
