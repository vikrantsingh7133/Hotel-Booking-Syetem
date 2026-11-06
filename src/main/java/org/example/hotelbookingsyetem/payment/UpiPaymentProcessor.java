package org.example.hotelbookingsyetem.payment;

import org.example.hotelbookingsyetem.model.PaymentMethod;
import org.springframework.stereotype.Component;

@Component
public class UpiPaymentProcessor extends PaymentProcessor {

    public UpiPaymentProcessor() {
        super(PaymentMethod.UPI, "vpa");
    }
}
