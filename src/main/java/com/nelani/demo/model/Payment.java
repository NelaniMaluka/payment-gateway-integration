package com.nelani.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Represents a payment in the system and encapsulates its state.
 * <p>
 * All state transitions (e.g., initiating, pending, success, failed, expired)
 * are
 * enforced through domain methods to maintain consistency.
 * <p>
 * Expiration is calculated internally and should not be modified directly.
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
    private UUID id;

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
    private OffsetDateTime createdAt;

    @NotNull
    @Future
    @Column(nullable = false)
    private OffsetDateTime expiresAt;

    @PastOrPresent
    private OffsetDateTime completedAt;

    @Setter
    private String providerReference;

    /**
     * Creates a new payment with a default expiration of 1 day.
     *
     * @param orderId  the unique order identifier
     * @param amount   the payment amount
     * @param status   the initial payment status
     * @param provider the payment provider
     */
    public Payment(String orderId, BigDecimal amount, PaymentStatus status, PaymentProviderType provider) {
        this.orderId = orderId;
        this.amount = amount;
        this.status = status;
        this.provider = provider;
        this.createdAt = OffsetDateTime.now();
        this.expiresAt = calculateExpiration();
    }

    /**
     * Returns true if the payment has expired according to its expiration date.
     *
     * @return true if expired, false otherwise
     */
    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(OffsetDateTime.now());
    }

    /**
     * Marks the payment as expired if it has passed its expiration date
     * and is not already expired.
     */
    public void expireIfNeeded() {
        if (status != PaymentStatus.EXPIRED && isExpired()) {
            this.status = PaymentStatus.EXPIRED;
        }
    }

    /**
     * Returns true if the payment can be resumed.
     * <p>
     * Only payments in the PENDING state that are not expired can be resumed.
     *
     * @return true if resumable, false otherwise
     */
    public boolean canBeResumed() {
        return status == PaymentStatus.PENDING && !isExpired();
    }

    /**
     * Returns true if the payment can be reinitialized.
     * <p>
     * Payments that are FAILED, EXPIRED, or expired by date can be reinitialized.
     *
     * @return true if reinitialized, false otherwise
     */
    public boolean canBeReinitialized() {
        return status == PaymentStatus.EXPIRED
                || status == PaymentStatus.FAILED
                || isExpired();
    }

    /**
     * Marks the payment as INITIATING.
     * <p>
     * Cannot be called if the payment is currently PENDING or SUCCESS.
     *
     * @throws IllegalStateException if the payment is active or successful
     */
    public void markInitiating() {
        if (status == PaymentStatus.PENDING || status == PaymentStatus.SUCCESS) {
            throw new IllegalStateException("Cannot re-initiate an active or successful payment");
        }
        this.status = PaymentStatus.INITIATING;
    }

    /**
     * Marks the payment as PENDING and sets the provider.
     * <p>
     * This should be called after creating a payment session with a provider.
     *
     * @param type the payment provider
     */
    public void markPending(PaymentProviderType type) {
        this.status = PaymentStatus.PENDING;
        this.provider = type;
    }

    /**
     * Marks the payment as FAILED.
     * <p>
     * Cannot fail a payment that is already a SUCCESS.
     *
     * @throws IllegalStateException if the payment is already successful
     */
    public void markFailed() {
        if (status == PaymentStatus.SUCCESS) {
            throw new IllegalStateException("Cannot fail a successful payment");
        }
        this.status = PaymentStatus.FAILED;
    }

    /**
     * Marks the payment as SUCCESS and records the completion time.
     */
    public void markSuccess() {
        this.status = PaymentStatus.SUCCESS;
        this.completedAt = OffsetDateTime.now();
    }

    /**
     * Calculates the default expiration for a payment.
     * <p>
     * Currently, the default lifetime is 1 day.
     *
     * @return the expiration date-time
     */
    private OffsetDateTime calculateExpiration() {
        return OffsetDateTime.now().plusDays(1);
    }
}
