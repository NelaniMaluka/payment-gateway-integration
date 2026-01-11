package com.nelani.demo.provider;

import com.nelani.demo.dto.PaymentResponseDTO;
import com.nelani.demo.dto.WebhookResult;
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
    public PaymentResponseDTO createPayment(Payment payment) {
        return null;
    }

    @Override
    public PaymentResponseDTO resumePayment(Payment payment) {
        return null;
    }

    @Override
    public WebhookResult handleWebhook(String payload, String signature) {
        return null;
    }
}
