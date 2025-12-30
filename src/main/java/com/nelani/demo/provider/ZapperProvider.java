package com.nelani.demo.provider;

import com.nelani.demo.dto.PaymentResponseDTO;
import com.nelani.demo.model.Payment;
import com.nelani.demo.model.PaymentProviderType;
import org.springframework.stereotype.Service;

// Polymorphism: Zapper-specific implementation of the PaymentProvider interface
@Service
public class ZapperProvider implements PaymentProvider {

    @Override
    public PaymentProviderType getType() {
        return PaymentProviderType.ZAPPER;
    }

    @Override
    public PaymentResponseDTO createPayment(Payment request) {
        return null;
    }

    @Override
    public void handleWebhook(String payload, String signature) {

    }
}
