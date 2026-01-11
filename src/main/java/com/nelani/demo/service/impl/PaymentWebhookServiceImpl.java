package com.nelani.demo.service.impl;

import com.nelani.demo.dto.WebhookResult;
import com.nelani.demo.model.Payment;
import com.nelani.demo.model.PaymentProviderType;
import com.nelani.demo.provider.PaymentProvider;
import com.nelani.demo.repository.PaymentRepository;
import com.nelani.demo.service.PaymentProviderFactory;
import com.nelani.demo.service.PaymentWebhookService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PaymentWebhookServiceImpl implements PaymentWebhookService {

    private final PaymentRepository paymentRepository;
    private final PaymentProviderFactory factory;

    public PaymentWebhookServiceImpl(PaymentRepository paymentRepository, PaymentProviderFactory factory) {
        this.paymentRepository = paymentRepository;
        this.factory = factory;
    }

    @Override
    @Transactional
    public void handleStripeWebhook(String payload, String signature) {
        // Verifies the webhook
        PaymentProvider provider = factory.get(PaymentProviderType.STRIPE);
        WebhookResult webhookResult = provider.handleWebhook(payload, signature);

        // Checks if the webhook is relevant
        if (!webhookResult.relevant()) {
            return;
        }

        // Checks if the paymentId is not null
        if (webhookResult.paymentId() == null) {
            return;
        }

        // Get the payment using the paymentId
        Payment payment = paymentRepository.findById(webhookResult.paymentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Payment not found"));

        // Updates the payment
        if (webhookResult.success()) {
            payment.markSuccess();
        } else {
            payment.markFailed();
        }

        paymentRepository.save(payment); // Save the payment
    }
}
