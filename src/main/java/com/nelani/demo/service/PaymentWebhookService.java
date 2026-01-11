package com.nelani.demo.service;

public interface PaymentWebhookService {

    void handleStripeWebhook(String payload, String signature);

}
