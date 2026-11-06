package org.example.hotelbookingsyetem.exception;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String resource, String id) {
        super(resource + " not found: " + id);
    }
}
