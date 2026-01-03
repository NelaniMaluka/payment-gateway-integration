package com.nelani.demo.provider;

import com.nelani.demo.dto.PaymentResponseDTO;
import com.nelani.demo.model.Payment;
import com.nelani.demo.model.PaymentProviderType;
import org.springframework.stereotype.Service;

// Polymorphism: PayPal-specific implementation of the PaymentProvider interface
@Service
public class PayPalProvider implements PaymentProvider {

    @Override
    public PaymentProviderType getType() {
        return PaymentProviderType.PAYPAL;
    }

    @Override
    public PaymentResponseDTO createPayment(Payment request) {
        return null;
    }

    @Override
    public void handleWebhook(String payload, String signature) {

    }
}
