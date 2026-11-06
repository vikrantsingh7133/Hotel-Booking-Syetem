package org.example.hotelbookingsyetem.exception;

/** Request is valid but clashes with current state (duplicates, no rooms, bad transitions). */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
