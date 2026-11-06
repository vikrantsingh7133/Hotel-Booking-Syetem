package org.example.hotelbookingsyetem.service;

import lombok.RequiredArgsConstructor;
import org.example.hotelbookingsyetem.model.Booking;
import org.example.hotelbookingsyetem.model.Payment;
import org.example.hotelbookingsyetem.model.PaymentMethod;
import org.example.hotelbookingsyetem.model.PaymentStatus;
import org.example.hotelbookingsyetem.exception.ConflictException;
import org.example.hotelbookingsyetem.exception.InvalidRequestException;
import org.example.hotelbookingsyetem.exception.NotFoundException;
import org.example.hotelbookingsyetem.payment.PaymentProcessor;
import org.example.hotelbookingsyetem.payment.PaymentResult;
import org.example.hotelbookingsyetem.repository.BookingRepository;
import org.example.hotelbookingsyetem.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final List<PaymentProcessor> processors;
    private final Clock clock;

    /**
     * Charges the booking and lets the result drive its state.
     * Same idempotency key again returns the first result without charging twice.
     */
    @Transactional
    public PaymentOutcome pay(String bookingId, String idempotencyKey, PaymentMethod method, Map<String, String> details) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new InvalidRequestException("idempotencyKey is required");
        }
        // Lock the booking so two payments for it cannot run at the same time.
        Booking booking = bookingRepository.findByIdForUpdate(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking", bookingId));

        Optional<Payment> previous = paymentRepository.findByIdempotencyKey(idempotencyKey);
        if (previous.isPresent()) {
            return replay(previous.get(), booking);
        }
        if (!booking.isAwaitingPayment()) {
            throw new ConflictException("Booking is " + booking.getStatus() + "; only PENDING_PAYMENT bookings can be paid");
        }
        // The expiry job may not have run yet, so check the deadline here too.
        LocalDateTime now = LocalDateTime.now(clock);
        if (booking.isHoldExpired(now)) {
            throw new ConflictException("Booking hold expired at " + booking.getExpiresAt() + "; please book again");
        }

        PaymentResult result = processorFor(method).charge(booking.getAmount(), details);

        PaymentStatus status = result.isSuccess() ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;
        Payment payment = paymentRepository.save(new Payment(
                bookingId, method, booking.getAmount(), status, idempotencyKey, result.getReference(), now));

        if (payment.isSuccessful()) {
            booking.confirm(now);
        } else {
            booking.markPaymentFailed(now);
        }
        return new PaymentOutcome(payment, booking);
    }

    private PaymentProcessor processorFor(PaymentMethod method) {
        for (PaymentProcessor processor : processors) {
            if (processor.getMethod() == method) {
                return processor;
            }
        }
        throw new InvalidRequestException("Unsupported payment method: " + method);
    }

    private PaymentOutcome replay(Payment previous, Booking booking) {
        if (!previous.getBookingId().equals(booking.getId())) {
            throw new ConflictException("idempotencyKey already used for another booking");
        }
        return new PaymentOutcome(previous, booking);
    }
}
