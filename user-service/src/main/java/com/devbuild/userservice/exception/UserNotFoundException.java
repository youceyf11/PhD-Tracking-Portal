package com.devbuild.userservice.exception;

// Custom Exceptions
class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
