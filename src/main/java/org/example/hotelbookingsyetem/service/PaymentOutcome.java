package org.example.hotelbookingsyetem.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.hotelbookingsyetem.model.Booking;
import org.example.hotelbookingsyetem.model.Payment;

@Getter
@AllArgsConstructor
public class PaymentOutcome {

    private final Payment payment;
    private final Booking booking;
}
