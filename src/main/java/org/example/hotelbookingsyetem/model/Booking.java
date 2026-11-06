package org.example.hotelbookingsyetem.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.hotelbookingsyetem.exception.InvalidRequestException;
import org.example.hotelbookingsyetem.exception.InvalidStateTransitionException;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "bookings", indexes = {
        @Index(columnList = "room_type_id, check_in, check_out"),
        @Index(columnList = "user_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Booking {

    public static final LocalTime CHECK_IN_TIME = LocalTime.NOON;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String propertyId;

    @Column(nullable = false)
    private String roomTypeId;

    private int guests;

    /** Number of rooms of {@link #roomTypeId} held by this booking. */
    private int rooms;

    @Column(nullable = false)
    private LocalDate checkIn;

    @Column(nullable = false)
    private LocalDate checkOut;

    /** Price fixed at booking time; later price changes do not affect it. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(precision = 12, scale = 2)
    private BigDecimal refundAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    private LocalDateTime createdAt;

    /** An unpaid booking holds its rooms only until this time. */
    private LocalDateTime expiresAt;

    private LocalDateTime updatedAt;

    public Booking(String userId, String propertyId, String roomTypeId, int guests, int rooms,
                   DateRange stay, BigDecimal amount, LocalDateTime now, Duration holdTime) {
        if (rooms < 1 || rooms > guests) {
            throw new InvalidRequestException("rooms must be between 1 and the number of guests");
        }
        this.userId = userId;
        this.propertyId = propertyId;
        this.roomTypeId = roomTypeId;
        this.guests = guests;
        this.rooms = rooms;
        this.checkIn = stay.getCheckIn();
        this.checkOut = stay.getCheckOut();
        this.amount = amount;
        this.status = BookingStatus.PENDING_PAYMENT;
        this.createdAt = now;
        this.updatedAt = now;
        this.expiresAt = now.plus(holdTime);
    }

    public DateRange getStay() {
        return new DateRange(checkIn, checkOut);
    }

    public LocalDateTime checkInTime() {
        return checkIn.atTime(CHECK_IN_TIME);
    }

    public boolean isOwnedBy(String userId) {
        return this.userId.equals(userId);
    }

    public boolean isAwaitingPayment() {
        return status == BookingStatus.PENDING_PAYMENT;
    }

    public boolean isHoldExpired(LocalDateTime now) {
        return isAwaitingPayment() && !now.isBefore(expiresAt);
    }

    public boolean isPaid() {
        return status == BookingStatus.CONFIRMED;
    }

    public void confirm(LocalDateTime now) {
        moveTo(BookingStatus.CONFIRMED, now);
    }

    public void markPaymentFailed(LocalDateTime now) {
        moveTo(BookingStatus.FAILED, now);
    }

    public void expire(LocalDateTime now) {
        moveTo(BookingStatus.EXPIRED, now);
    }

    public void cancel(BigDecimal refundAmount, LocalDateTime now) {
        moveTo(BookingStatus.CANCELLED, now);
        this.refundAmount = refundAmount;
    }

    private void moveTo(BookingStatus next, LocalDateTime now) {
        if (!status.canMoveTo(next)) {
            throw new InvalidStateTransitionException(status, next);
        }
        this.status = next;
        this.updatedAt = now;
    }
}
