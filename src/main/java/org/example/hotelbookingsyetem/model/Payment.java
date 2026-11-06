package org.example.hotelbookingsyetem.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String bookingId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(nullable = false, unique = true)
    private String idempotencyKey;

    private String gatewayRef;

    private LocalDateTime createdAt;

    public Payment(String bookingId, PaymentMethod method, BigDecimal amount, PaymentStatus status,
                   String idempotencyKey, String gatewayRef, LocalDateTime now) {
        this.bookingId = bookingId;
        this.method = method;
        this.amount = amount;
        this.status = status;
        this.idempotencyKey = idempotencyKey;
        this.gatewayRef = gatewayRef;
        this.createdAt = now;
    }

    public boolean isSuccessful() {
        return status == PaymentStatus.SUCCESS;
    }
}
