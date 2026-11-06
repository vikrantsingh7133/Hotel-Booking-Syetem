package org.example.hotelbookingsyetem.payment;

import org.example.hotelbookingsyetem.model.PaymentMethod;
import org.springframework.stereotype.Component;

@Component
public class WalletPaymentProcessor extends PaymentProcessor {

    public WalletPaymentProcessor() {
        super(PaymentMethod.WALLET, "walletId");
    }
}
