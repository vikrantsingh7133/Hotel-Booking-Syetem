package org.example.hotelbookingsyetem.payment;

import org.example.hotelbookingsyetem.model.PaymentMethod;
import org.springframework.stereotype.Component;

@Component
public class CardPaymentProcessor extends PaymentProcessor {

    public CardPaymentProcessor() {
        super(PaymentMethod.CARD, "cardNumber");
    }
}
