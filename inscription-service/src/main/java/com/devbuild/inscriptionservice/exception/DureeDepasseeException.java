package com.devbuild.inscriptionservice.exception;

public class DureeDepasseeException extends RuntimeException {
    private final long anneesDepuisInscription;
    private final boolean depasseLimiteMaximale;

    public DureeDepasseeException(String message, long anneesDepuisInscription, boolean depasseLimiteMaximale) {
        super(message);
        this.anneesDepuisInscription = anneesDepuisInscription;
        this.depasseLimiteMaximale = depasseLimiteMaximale;
    }

    public long getAnneesDepuisInscription() {
        return anneesDepuisInscription;
    }

    public boolean isDepasseLimiteMaximale() {
        return depasseLimiteMaximale;
    }}
