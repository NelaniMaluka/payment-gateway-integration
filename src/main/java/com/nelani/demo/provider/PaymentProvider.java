package com.nelani.demo.provider;

import com.nelani.demo.dto.PaymentResponseDTO;
import com.nelani.demo.dto.WebhookResult;
import com.nelani.demo.model.Payment;
import com.nelani.demo.model.PaymentProviderType;

/**
 * Common abstraction for all payment providers.
 * Implementations must handle provider-specific concerns internally
 * (retries, idempotency, error handling).
 */
public interface PaymentProvider {

    /**
     * @return the provider type (e.g. STRIPE, PAYPAL)
     */
    PaymentProviderType getType();

    /**
     * Creates a new payment session with the provider.
     *
     * @param payment domain payment object
     * @return provider response containing client-facing payment details
     */
    PaymentResponseDTO createPayment(Payment payment);

    /**
     * Indicates whether this provider supports resuming payment sessions.
     *
     * Defaults too false to be safe.
     */
    default boolean supportsResume() {
        return false;
    }

    /**
     * Resumes an existing payment session.
     *
     * @param payment domain payment object
     * @return provider response containing updated payment details
     */
    PaymentResponseDTO resumePayment(Payment payment);

    /**
     * Handles incoming provider webhooks.
     *
     * @param payload   raw webhook payload
     * @param signature provider signature header
     * @return webhook processing result
     */
    WebhookResult handleWebhook(String payload, String signature);
}
