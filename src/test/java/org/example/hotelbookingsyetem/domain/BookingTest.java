package org.example.hotelbookingsyetem.domain;

import org.example.hotelbookingsyetem.exception.InvalidStateTransitionException;
import org.example.hotelbookingsyetem.model.Booking;
import org.example.hotelbookingsyetem.model.BookingStatus;
import org.example.hotelbookingsyetem.model.DateRange;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BookingTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2030, 1, 1, 10, 0);

    private Booking newBooking() {
        DateRange stay = new DateRange(LocalDate.of(2030, 1, 10), LocalDate.of(2030, 1, 12));
        return new Booking("user", "property", "room", 2, 1, stay, new BigDecimal("4000"), NOW, Duration.ofMinutes(10));
    }

    @Test
    void startsAwaitingPayment() {
        assertThat(newBooking().getStatus()).isEqualTo(BookingStatus.PENDING_PAYMENT);
    }

    @Test
    void confirmedBookingCanBeCancelledWithRefund() {
        Booking booking = newBooking();
        booking.confirm(NOW);
        booking.cancel(new BigDecimal("2000"), NOW);

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(booking.getRefundAmount()).isEqualByComparingTo("2000");
    }

    @Test
    void failedBookingCannotBeConfirmed() {
        Booking booking = newBooking();
        booking.markPaymentFailed(NOW);

        assertThatThrownBy(() -> booking.confirm(NOW)).isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void cancelledBookingCannotBeCancelledAgain() {
        Booking booking = newBooking();
        booking.cancel(BigDecimal.ZERO, NOW);

        assertThatThrownBy(() -> booking.cancel(BigDecimal.ZERO, NOW))
                .isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void holdExpiresAfterHoldTime() {
        Booking booking = newBooking();

        assertThat(booking.getExpiresAt()).isEqualTo(NOW.plusMinutes(10));
        assertThat(booking.isHoldExpired(NOW.plusMinutes(9))).isFalse();
        assertThat(booking.isHoldExpired(NOW.plusMinutes(10))).isTrue();
    }

    @Test
    void expiredBookingCannotBeConfirmed() {
        Booking booking = newBooking();
        booking.expire(NOW);

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.EXPIRED);
        assertThatThrownBy(() -> booking.confirm(NOW)).isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void paidBookingCannotExpire() {
        Booking booking = newBooking();
        booking.confirm(NOW);

        assertThat(booking.isHoldExpired(NOW.plusDays(1))).isFalse();
        assertThatThrownBy(() -> booking.expire(NOW)).isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void onlyPendingAndConfirmedHoldARoom() {
        assertThat(BookingStatus.holdingRoom())
                .containsExactlyInAnyOrder(BookingStatus.PENDING_PAYMENT, BookingStatus.CONFIRMED);
    }
}
