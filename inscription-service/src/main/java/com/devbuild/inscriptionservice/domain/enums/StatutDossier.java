package com.devbuild.inscriptionservice.domain.enums;

public enum StatutDossier {

    EN_ATTENTE_DIRECTEUR("En attente de validation du directeur"),
    EN_ATTENTE_ADMIN("En attente de validation administrative"),
    VALIDE("Dossier validé"),
    REJETE("Dossier rejeté");

    private final String description;

    StatutDossier(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isTerminal() {
        return this == VALIDE || this == REJETE;
    }

    public boolean isPending() {
        return this == EN_ATTENTE_DIRECTEUR || this == EN_ATTENTE_ADMIN;
    }
}
