package com.devbuild.inscriptionservice.exception;

public class CampagneNotActiveException extends RuntimeException {
    public CampagneNotActiveException(String message) {
        super(message);
    }

    public CampagneNotActiveException(String message, Throwable cause) {
        super(message, cause);
    }}
