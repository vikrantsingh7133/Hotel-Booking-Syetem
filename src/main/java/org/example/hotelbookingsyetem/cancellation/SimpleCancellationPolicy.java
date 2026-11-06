package org.example.hotelbookingsyetem.cancellation;

import org.example.hotelbookingsyetem.model.Booking;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 48h+ before check-in: full refund. 24h+: half. Later: nothing.
 */
@Component
public class SimpleCancellationPolicy implements CancellationPolicy {

    private static final BigDecimal FULL = BigDecimal.ONE;
    private static final BigDecimal HALF = new BigDecimal("0.5");

    @Override
    public BigDecimal refundFor(Booking booking, LocalDateTime now) {
        long hoursLeft = Duration.between(now, booking.checkInTime()).toHours();
        return booking.getAmount()
                .multiply(refundShare(hoursLeft))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal refundShare(long hoursLeft) {
        if (hoursLeft >= 48) {
            return FULL;
        }
        if (hoursLeft >= 24) {
            return HALF;
        }
        return BigDecimal.ZERO;
    }
}
