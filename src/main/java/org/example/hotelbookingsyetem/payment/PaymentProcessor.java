package org.example.hotelbookingsyetem.payment;

import org.example.hotelbookingsyetem.exception.InvalidRequestException;
import org.example.hotelbookingsyetem.model.PaymentMethod;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * One subclass per payment method. To add a method, add an enum value and a subclass.
 *
 * Charging is mocked here: no real gateway is called. A real integration
 * would override {@link #charge}.
 */
public abstract class PaymentProcessor {

    private final PaymentMethod method;
    private final String requiredDetail;

    protected PaymentProcessor(PaymentMethod method, String requiredDetail) {
        this.method = method;
        this.requiredDetail = requiredDetail;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    /** Mock: declines when the payer detail contains "fail", e.g. vpa "fail@upi". */
    public PaymentResult charge(BigDecimal amount, Map<String, String> details) {
        String payer = details == null ? null : details.get(requiredDetail);
        if (payer == null || payer.isBlank()) {
            throw new InvalidRequestException("Payment detail '" + requiredDetail + "' is required");
        }
        boolean success = !payer.toLowerCase().contains("fail");
        return new PaymentResult(success, "MOCK-" + UUID.randomUUID());
    }
}
