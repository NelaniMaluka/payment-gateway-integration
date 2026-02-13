package com.nelani.demo.provider;

import com.nelani.demo.dto.PaymentResponseDTO;
import com.nelani.demo.dto.WebhookResult;
import com.nelani.demo.exception.PaymentProviderTemporaryException;
import com.nelani.demo.model.Payment;
import com.nelani.demo.model.PaymentProviderType;
import com.paypal.core.PayPalHttpClient;
import com.paypal.core.PayPalEnvironment;
import com.paypal.orders.AmountWithBreakdown;
import com.paypal.orders.OrderRequest;
import com.paypal.orders.OrdersCreateRequest;
import com.paypal.orders.PurchaseUnitRequest;
import com.paypal.orders.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

// Polymorphism: PayPal-specific implementation of the PaymentProvider interface
@Log4j2
@Service
public class PayPalProvider implements PaymentProvider {

        @Value("${paypal.client-id}")
        private String clientId;

        @Value("${paypal.client-secret}")
        private String clientSecret;

        private PayPalHttpClient client;

        @PostConstruct
        public void init() {
                PayPalEnvironment env = new PayPalEnvironment.Sandbox(clientId, clientSecret);
                client = new PayPalHttpClient(env);
        }

        @Override
        public PaymentProviderType getType() {
                return PaymentProviderType.PAYPAL;
        }

        @Override
        @Retryable(retryFor = PaymentProviderTemporaryException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
        public PaymentResponseDTO createPayment(Payment payment) {

                // High-level audit log: helps trace payment flow in prod logs
                log.info("Creating PayPal order [paymentId={}, orderId={}]",
                                payment.getId(), payment.getOrderId());

                try {

                        // Defines intent: CAPTURE means immediate payment capture after approval
                        OrderRequest orderRequest = new OrderRequest();
                        orderRequest.checkoutPaymentIntent("CAPTURE");

                        // Controls checkout experience shown to the user on PayPal
                        orderRequest.applicationContext(
                                        new ApplicationContext()
                                                        .brandName("Nelani Store")
                                                        .userAction("PAY_NOW")
                                                        .shippingPreference("NO_SHIPPING"));

                        // Core monetary object used consistently across PayPal structures
                        Money money = new Money()
                                        .currencyCode("USD")
                                        .value(payment.getAmount().toPlainString());

                        // Represents a single logical item on the PayPal invoice
                        Item item = new Item()
                                        .name("Order " + payment.getOrderId())
                                        .quantity("1")
                                        .unitAmount(money);

                        // Explicit breakdown avoids rounding mismatches on PayPal side
                        AmountBreakdown breakdown = new AmountBreakdown()
                                        .itemTotal(money);

                        // Final amount PayPal will charge
                        AmountWithBreakdown amount = new AmountWithBreakdown()
                                        .currencyCode("USD")
                                        .value(payment.getAmount().toPlainString())
                                        .amountBreakdown(breakdown);

                        PurchaseUnitRequest purchaseUnit = new PurchaseUnitRequest()
                                        .referenceId(payment.getOrderId())
                                        .customId(payment.getId().toString())
                                        .invoiceId("INV-" + payment.getOrderId())
                                        .description("Order payment")
                                        .amountWithBreakdown(amount)
                                        .items(List.of(item));

                        orderRequest.purchaseUnits(List.of(purchaseUnit));

                        OrdersCreateRequest request = new OrdersCreateRequest().requestBody(orderRequest);

                        // Remote API call to PayPal
                        Order order = client.execute(request).result();

                        log.info("PayPal order created successfully [paypalOrderId={}]", order.id());

                        return new PaymentResponseDTO(
                                        payment.getOrderId(),
                                        order.id(),
                                        order.id(),
                                        payment.getAmount(),
                                        getType(),
                                        null,
                                        null,
                                        null,
                                        null);

                }
                // Network issues, timeouts, DNS, SSL handshake problems, etc.
                catch (IOException e) {
                        log.warn("PayPal network failure [paymentId={}, orderId={}]",
                                        payment.getId(), payment.getOrderId(), e);

                        throw new PaymentProviderTemporaryException(
                                        "Unable to reach PayPal at the moment. Please try again shortly.");
                }
                // Validation / programming errors (nulls, invalid amounts, bad state)
                catch (IllegalArgumentException | IllegalStateException e) {
                        log.error("Invalid payment state [paymentId={}, orderId={}]",
                                        payment.getId(), payment.getOrderId(), e);

                        throw new IllegalStateException(
                                        "Payment could not be created due to invalid payment data.");
                } catch (Exception e) {
                        log.error("Unexpected PayPal failure [paymentId={}, orderId={}]",
                                        payment.getId(), payment.getOrderId(), e);

                        throw new RuntimeException(
                                        "Payment could not be processed due to an internal error.");
                }
        }

        @Override
        public boolean supportsResume() {
                return true;
        }

        @Override
        @Retryable(retryFor = PaymentProviderTemporaryException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
        public PaymentResponseDTO resumePayment(Payment payment) {
                return null;
        }

        @Override
        public WebhookResult handleWebhook(String payload, String signature) {
                return null;
        }

        @Recover
        public PaymentResponseDTO recover(
                        PaymentProviderTemporaryException ex,
                        Payment payment) {
                log.error("PayPal failed after retries. paymentId={}", payment.getId(), ex);

                throw new RuntimeException(
                                "Payment service is temporarily unavailable. Please try again later.");
        }

}
