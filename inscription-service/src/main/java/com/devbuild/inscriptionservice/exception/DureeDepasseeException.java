package com.devbuild.inscriptionservice.exception;

import lombok.Getter;

@Getter
public class DureeDepasseeException extends RuntimeException {
    private final long anneesDepuisInscription;
    private final boolean depasseLimiteMaximale;

    public DureeDepasseeException(String message, long anneesDepuisInscription, boolean depasseLimiteMaximale) {
        super(message);
        this.anneesDepuisInscription = anneesDepuisInscription;
        this.depasseLimiteMaximale = depasseLimiteMaximale;
    }

    public DureeDepasseeException(String message, long anneesDepuisInscription, boolean depasseLimiteMaximale, Throwable cause) {
        super(message, cause);
        this.anneesDepuisInscription = anneesDepuisInscription;
        this.depasseLimiteMaximale = depasseLimiteMaximale;
    }
}