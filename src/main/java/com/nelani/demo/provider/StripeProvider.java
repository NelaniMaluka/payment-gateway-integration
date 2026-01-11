package com.nelani.demo.provider;

import com.nelani.demo.dto.PaymentResponseDTO;
import com.nelani.demo.dto.WebhookResult;
import com.nelani.demo.exception.PaymentProviderTemporaryException;
import com.nelani.demo.model.Payment;
import com.nelani.demo.model.PaymentProviderType;
import com.stripe.Stripe;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.RequestOptions;
import com.stripe.net.Webhook;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

// Polymorphism: Stripe-specific implementation of the PaymentProvider interface
@Log4j2
@Service
public class StripeProvider implements PaymentProvider {

        @Value("${stripe.secret-key}")
        private String stripeSecretKey;

        @Value("${stripe.secret-webhook}")
        private String stripeSecretWebhook;

        @PostConstruct
        public void init() {
                Stripe.apiKey = stripeSecretKey;
        }

        @Override
        public PaymentProviderType getType() {
                return PaymentProviderType.STRIPE;
        }

        @Override
        @Retryable(retryFor = PaymentProviderTemporaryException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
        public PaymentResponseDTO createPayment(Payment payment) {

                log.info("Creating Stripe payment [paymentId={}, orderId={}]",
                                payment.getId(), payment.getOrderId());

                try {
                        /*
                         * Create or register the customer with Stripe.
                         * NOTE:
                         * - In production, customer details should come from your domain model,
                         * not hard-coded values.
                         * - Do NOT log email addresses or personal information.
                         */
                        Map<String, Object> customerParams = new HashMap<>();
                        customerParams.put("email", "johndoe@gmail.com");
                        customerParams.put("name", "John Doe");

                        final Customer customer = Customer.create(customerParams);

                        log.debug("Stripe customer created [customerId={}]", customer.getId());

                        /*
                         * Build PaymentIntent parameters.
                         * Stripe expects amount in the smallest currency unit (cents).
                         */
                        Map<String, Object> params = new HashMap<>();
                        params.put(
                                        "amount",
                                        payment.getAmount()
                                                        .multiply(BigDecimal.valueOf(100))
                                                        .setScale(0, RoundingMode.HALF_UP)
                                                        .longValueExact());
                        params.put("currency", "zar");
                        params.put("customer", customer.getId());
                        params.put("receipt_email", "JohnDoe@gmail.com");
                        params.put("description", "Order " + payment.getOrderId());
                        params.put("statement_descriptor_suffix", "NELANI");
                        params.put("automatic_payment_methods", Map.of("enabled", true));

                        int itemCount = ThreadLocalRandom.current().nextInt(1, 6);

                        /*
                         * Metadata is safe for internal tracking and reconciliation.
                         * Never store sensitive card or user data here.
                         */
                        params.put("metadata", Map.of(
                                        "orderId", payment.getOrderId(),
                                        "paymentId", payment.getId(),
                                        "itemCount", String.valueOf(itemCount),
                                        "expiresAt", payment.getExpiresAt().toString()));

                        /*
                         * Idempotency ensures Stripe does not create duplicate payments
                         * if retries occur (network issues, timeouts, etc.).
                         */
                        RequestOptions options = RequestOptions.builder()
                                        .setIdempotencyKey("payment-" + payment.getId())
                                        .build();

                        // Create the PaymentIntent with Stripe
                        final PaymentIntent intent = PaymentIntent.create(params, options);

                        log.info(
                                        "Stripe payment intent created successfully [paymentId={}, intentId={}]",
                                        payment.getId(),
                                        intent.getId());

                        /*
                         * Return only non-sensitive data to the caller.
                         * clientSecret must only be sent to the frontend over HTTPS.
                         */
                        return new PaymentResponseDTO(
                                        payment.getOrderId(),
                                        intent.getId(),
                                        intent.getClientSecret(),
                                        payment.getAmount(),
                                        getType(),
                                        null,
                                        null,
                                        null,
                                        null);

                } catch (InvalidRequestException e) {
                        /*
                         * Indicates a bug or invalid request sent to Stripe.
                         * This should NOT be retried.
                         */
                        log.error(
                                        "Invalid Stripe payment request [paymentId={}, orderId={}]",
                                        payment.getId(),
                                        payment.getOrderId(),
                                        e);
                        throw new IllegalArgumentException(
                                        "Invalid payment request sent to provider");

                } catch (AuthenticationException e) {
                        /*
                         * Indicates a fatal configuration error (invalid API key, environment
                         * misconfiguration).
                         * This should be alert immediately and must not be retried.
                         */
                        log.error(
                                        "Stripe authentication/configuration failure",
                                        e);
                        throw new IllegalStateException(
                                        "Payment provider configuration error");

                } catch (StripeException e) {
                        /*
                         * Represents transient Stripe failures (timeouts, 5xx errors).
                         * These are safe to retry using Spring Retry.
                         */
                        log.warn(
                                        "Transient Stripe failure, retrying [paymentId={}, orderId={}]",
                                        payment.getId(),
                                        payment.getOrderId(),
                                        e);
                        throw new PaymentProviderTemporaryException(
                                        "Stripe payment temporarily unavailable");
                }
        }

        @Override
        @Retryable(retryFor = PaymentProviderTemporaryException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
        public PaymentResponseDTO resumePayment(Payment payment) {

                log.info(
                                "Resuming Stripe payment [paymentId={}, orderId={}, providerRef={}]",
                                payment.getId(),
                                payment.getOrderId(),
                                payment.getProviderReference());

                try {
                        /*
                         * Retrieve the existing PaymentIntent from Stripe.
                         * This does not create a new payment and is safe to retry.
                         */
                        final PaymentIntent intent = PaymentIntent.retrieve(
                                        payment.getProviderReference());

                        log.debug(
                                        "Stripe payment intent retrieved [intentId={}, status={}]",
                                        intent.getId(),
                                        intent.getStatus());

                        /*
                         * Safety check:
                         * If Stripe already marked the payment as succeeded,
                         * resuming it would be a logic error in our system.
                         * This should not be retried.
                         */
                        if ("succeeded".equals(intent.getStatus())) {
                                log.warn(
                                                "Attempt to resume already completed payment [paymentId={}, intentId={}]",
                                                payment.getId(),
                                                intent.getId());
                                throw new IllegalStateException("Payment already completed");
                        }

                        log.info(
                                        "Stripe payment resumed successfully [paymentId={}, intentId={}]",
                                        payment.getId(),
                                        intent.getId());

                        /*
                         * Return non-sensitive data only.
                         * clientSecret must be delivered to the frontend securely.
                         */
                        return new PaymentResponseDTO(
                                        payment.getOrderId(),
                                        intent.getId(),
                                        intent.getClientSecret(),
                                        payment.getAmount(),
                                        getType(),
                                        null,
                                        null,
                                        null,
                                        null);

                } catch (StripeException e) {
                        /*
                         * Represents transient Stripe failures (timeouts, API issues).
                         * Safe to retry using Spring Retry.
                         */
                        log.warn(
                                        "Transient Stripe failure while resuming payment [paymentId={}, orderId={}]",
                                        payment.getId(),
                                        payment.getOrderId(),
                                        e);
                        throw new PaymentProviderTemporaryException(
                                        "Stripe payment temporarily unavailable");
                }
        }

        @Override
        public boolean supportsResume() {
                return true;
        }

        @Override
        public WebhookResult handleWebhook(String payload, String signature) {

                Event event;

                /*
                 * Verify webhook authenticity using Stripe's signing secret.
                 * This ensures the payload was sent by Stripe and not a third party.
                 * Never log the raw payload or signature.
                 */
                try {
                        event = Webhook.constructEvent(
                                        payload,
                                        signature,
                                        stripeSecretWebhook);
                } catch (SignatureVerificationException e) {
                        /*
                         * Invalid signatures indicate either a misconfiguration
                         * or a potential malicious request.
                         * Do not retry or process the event further.
                         */
                        log.warn("Invalid Stripe webhook signature");
                        throw new IllegalArgumentException("Invalid webhook signature");
                }

                log.info(
                                "Stripe webhook received [eventId={}, type={}]",
                                event.getId(),
                                event.getType());

                PaymentIntent intent;
                boolean success;

                /*
                 * Handle only the payment lifecycle events that affect
                 * internal payment state. All other events are safely ignored.
                 */
                switch (event.getType()) {

                        case "payment_intent.succeeded" -> {
                                intent = extractIntent(event);
                                success = true;
                        }

                        case "payment_intent.payment_failed",
                                        "payment_intent.canceled" -> {
                                intent = extractIntent(event);
                                success = false;
                        }

                        default -> {
                                log.debug(
                                                "Unhandled Stripe webhook event type [type={}]",
                                                event.getType());
                                return new WebhookResult(null, false, false);
                        }
                }

                /*
                 * Metadata is the only reliable link between Stripe events
                 * and internal payment records.
                 */
                Map<String, String> metadata = intent.getMetadata();
                if (metadata == null || !metadata.containsKey("paymentId")) {
                        log.warn(
                                        "Stripe webhook missing paymentId metadata [intentId={}]",
                                        intent.getId());
                        return new WebhookResult(null, false, false);
                }

                String paymentIdRaw = metadata.get("paymentId");

                /*
                 * Defensive validation of paymentId.
                 * Malformed metadata should not break webhook processing.
                 */
                UUID paymentId;
                try {
                        paymentId = UUID.fromString(paymentIdRaw);
                } catch (IllegalArgumentException e) {
                        log.warn(
                                        "Invalid paymentId UUID in Stripe metadata [value={}, intentId={}]",
                                        paymentIdRaw,
                                        intent.getId());
                        return new WebhookResult(null, false, false);
                }

                log.info(
                                "Stripe webhook processed successfully [paymentId={}, success={}]",
                                paymentId,
                                success);

                /*
                 * The third flag indicates the event was verified and handled
                 * (even if the payment failed).
                 */
                return new WebhookResult(paymentId, success, true);
        }

        @Recover
        public PaymentResponseDTO recover(
                        PaymentProviderTemporaryException ex,
                        Payment payment) {
                log.error("Stripe failed after retries. paymentId={}", payment.getId(), ex);

                throw new RuntimeException(
                                "Payment service is temporarily unavailable. Please try again later.");
        }

        private PaymentIntent extractIntent(Event event) {
                return (PaymentIntent) event
                                .getDataObjectDeserializer()
                                .getObject()
                                .orElseThrow(() -> new IllegalStateException("Missing PaymentIntent in webhook event"));
        }

}
