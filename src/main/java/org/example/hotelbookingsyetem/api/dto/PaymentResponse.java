package org.example.hotelbookingsyetem.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.hotelbookingsyetem.model.BookingStatus;
import org.example.hotelbookingsyetem.model.Payment;
import org.example.hotelbookingsyetem.model.PaymentMethod;
import org.example.hotelbookingsyetem.model.PaymentStatus;
import org.example.hotelbookingsyetem.service.PaymentOutcome;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class PaymentResponse {

    private final String paymentId;
    private final String bookingId;
    private final PaymentMethod method;
    private final BigDecimal amount;
    private final PaymentStatus status;
    private final String gatewayRef;
    private final BookingStatus bookingStatus;

    public static PaymentResponse from(PaymentOutcome outcome) {
        Payment p = outcome.getPayment();
        return new PaymentResponse(p.getId(), p.getBookingId(), p.getMethod(), p.getAmount(),
                p.getStatus(), p.getGatewayRef(), outcome.getBooking().getStatus());
    }
}
