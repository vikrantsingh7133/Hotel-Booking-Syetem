package org.example.hotelbookingsyetem.exception;

import org.example.hotelbookingsyetem.model.BookingStatus;

public class InvalidStateTransitionException extends ConflictException {

    public InvalidStateTransitionException(BookingStatus from, BookingStatus to) {
        super("Booking cannot move from " + from + " to " + to);
    }
}
