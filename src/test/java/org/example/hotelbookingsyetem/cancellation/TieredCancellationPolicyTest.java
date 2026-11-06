package org.example.hotelbookingsyetem.cancellation;

import org.example.hotelbookingsyetem.model.Booking;
import org.example.hotelbookingsyetem.model.DateRange;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class TieredCancellationPolicyTest {

    private final SimpleCancellationPolicy policy = new SimpleCancellationPolicy();

    /** Check-in is 2030-01-10 at 12:00. */
    @ParameterizedTest
    @CsvSource({
            "2030-01-08T12:00, 1000.00",   // exactly 48h before → full
            "2030-01-08T12:01, 500.00",    // just under 48h → half
            "2030-01-09T12:00, 500.00",    // exactly 24h → half
            "2030-01-09T12:01, 0.00",      // under 24h → nothing
    })
    void refundDependsOnHoursBeforeCheckIn(LocalDateTime cancelAt, BigDecimal expectedRefund) {
        DateRange stay = new DateRange(LocalDate.of(2030, 1, 10), LocalDate.of(2030, 1, 11));
        Booking booking = new Booking("u", "p", "r", 1, 1, stay, new BigDecimal("1000"), cancelAt, Duration.ofMinutes(10));

        assertThat(policy.refundFor(booking, cancelAt)).isEqualByComparingTo(expectedRefund);
    }
}
