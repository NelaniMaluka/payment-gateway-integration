package com.nelani.demo.provider;

import com.nelani.demo.dto.PaymentResponseDTO;
import com.nelani.demo.model.Payment;
import com.nelani.demo.model.PaymentProviderType;
import org.springframework.stereotype.Service;

// Polymorphism: Ozow-specific implementation of the PaymentProvider interface
@Service
public class OzowProvider implements PaymentProvider {

    @Override
    public PaymentProviderType getType() {
        return PaymentProviderType.OZOW;
    }

    @Override
    public PaymentResponseDTO createPayment(Payment request) {
        return null;
    }

    @Override
    public void handleWebhook(String payload, String signature) {

    }
}
