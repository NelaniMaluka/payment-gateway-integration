package com.nelani.demo.provider;

import com.nelani.demo.dto.PaymentResponseDTO;
import com.nelani.demo.model.Payment;
import com.nelani.demo.model.PaymentProviderType;

// Defines a common abstraction for all payment providers.
// Exposes required operations while hiding provider-specific implementations.
public interface PaymentProvider {

    PaymentProviderType getType();

    PaymentResponseDTO createPayment(Payment request);

    void handleWebhook(String payload, String signature);

}
