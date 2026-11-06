package org.example.hotelbookingsyetem.model;

import java.util.EnumSet;
import java.util.Set;

public enum BookingStatus {
    PENDING_PAYMENT,
    CONFIRMED,
    FAILED,
    CANCELLED,
    /** Not paid within the hold time. */
    EXPIRED;

    public boolean canMoveTo(BookingStatus next) {
        return switch (this) {
            case PENDING_PAYMENT -> next == CONFIRMED || next == FAILED || next == CANCELLED || next == EXPIRED;
            case CONFIRMED -> next == CANCELLED;
            case FAILED, CANCELLED, EXPIRED -> false;
        };
    }

    /** Statuses in which a booking occupies a room. */
    public static Set<BookingStatus> holdingRoom() {
        return EnumSet.of(PENDING_PAYMENT, CONFIRMED);
    }
}
