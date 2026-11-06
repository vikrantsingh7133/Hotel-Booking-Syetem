package org.example.hotelbookingsyetem.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.hotelbookingsyetem.model.PaymentMethod;

import java.util.Map;

/**
 * {@code idempotencyKey}: client-generated id, reused on retries so the booking is charged once.
 * {@code details} depend on the method: CARD → cardNumber, UPI → vpa, WALLET → walletId.
 */
@Getter
@Setter
@NoArgsConstructor
public class PayRequest {

    @NotBlank
    private String idempotencyKey;

    @NotNull
    private PaymentMethod method;

    private Map<String, String> details;
}
