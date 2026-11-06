package org.example.hotelbookingsyetem.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentResult {

    private final boolean success;
    private final String reference;
}
