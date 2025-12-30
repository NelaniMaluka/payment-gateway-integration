package com.nelani.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Encapsulates payment state and enforces valid state transitions.
 * Internal fields are private and can only be modified through
 * domain-specific methods to maintain consistency.
 */
@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_payment_order", columnList = "orderId"),
        @Index(name = "idx_payment_status", columnList = "status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private Long id;

    @NotBlank(message = "Order ID is required")
    @Size(max = 100, message = "Order ID must not exceed 100 characters")
    @Column(nullable = false, unique = true)
    private String orderId;

    @NotNull(message = "Payment amount is required")
    @DecimalMin(value = "0.01", message = "Payment amount must be greater than zero")
    @Digits(integer = 10, fraction = 2, message = "Amount must have up to 2 decimal places")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @NotNull(message = "Payment status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @NotNull(message = "Payment provider is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentProviderType provider;

    @NotNull
    @PastOrPresent
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @NotNull
    @Future
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @PastOrPresent
    private LocalDateTime completedAt;

    public Payment(String orderId, BigDecimal amount, PaymentStatus status, PaymentProviderType provider,
            LocalDateTime expiresAt) {
        this.orderId = orderId;
        this.amount = amount;
        this.status = status;
        this.provider = provider;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = expiresAt;
    }

    public void markInitiating() {
        if (status == PaymentStatus.PENDING || status == PaymentStatus.SUCCESS) {
            throw new IllegalStateException("Cannot re-initiate an active or successful payment");
        }
        this.status = PaymentStatus.INITIATING;
    }

    public void markPending(PaymentProviderType type) {
        this.status = PaymentStatus.PENDING;
        this.provider = type;
    }

    public void markFailed() {
        if (status == PaymentStatus.SUCCESS) {
            throw new IllegalStateException("Cannot fail a successful payment");
        }
        this.status = PaymentStatus.FAILED;
    }

    public void markSuccess() {
        this.status = PaymentStatus.SUCCESS;
        this.completedAt = LocalDateTime.now();
    }

    public boolean canRetry() {
        return status == PaymentStatus.FAILED || status == PaymentStatus.INITIATING;
    }

}
