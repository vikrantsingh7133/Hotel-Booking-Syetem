package org.example.hotelbookingsyetem.cancellation;

import org.example.hotelbookingsyetem.model.Booking;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface CancellationPolicy {

    /** Amount to give back when a paid booking is cancelled at {@code now}. */
    BigDecimal refundFor(Booking booking, LocalDateTime now);
}
