package com.nelani.demo.service;

import com.nelani.demo.dto.WebhookResult;
import com.nelani.demo.model.Payment;
import com.nelani.demo.model.PaymentProviderType;
import com.nelani.demo.provider.PaymentProvider;
import com.nelani.demo.repository.PaymentRepository;
import com.nelani.demo.service.impl.PaymentWebhookServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PaymentWebhookServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentProviderFactory factory;

    @Mock
    private PaymentProvider provider;

    @InjectMocks
    private PaymentWebhookServiceImpl paymentWebhookService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void handleStripeWebhook_updatesPaymentToSuccess_whenWebhookIsRelevantAndSuccessful() {
        // Arrange
        UUID paymentId = UUID.randomUUID();
        Payment payment = new Payment(
                "order-1",
                BigDecimal.valueOf(100),
                com.nelani.demo.model.PaymentStatus.PENDING,
                PaymentProviderType.STRIPE
        );

        WebhookResult webhookResult = new WebhookResult(
                paymentId,
                true,
                true
        );

        when(factory.get(PaymentProviderType.STRIPE)).thenReturn(provider);
        when(provider.handleWebhook(any(), any())).thenReturn(webhookResult);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        // Act
        paymentWebhookService.handleStripeWebhook("payload", "signature");

        // Assert
        verify(paymentRepository).save(payment);
    }

    @Test
    void handleStripeWebhook_updatesPaymentToFailed_whenWebhookIsRelevantAndFailed() {
        // Arrange
        UUID paymentId = UUID.randomUUID();
        Payment payment = new Payment(
                "order-1",
                BigDecimal.valueOf(100),
                com.nelani.demo.model.PaymentStatus.PENDING,
                PaymentProviderType.STRIPE
        );

        WebhookResult webhookResult = new WebhookResult(
                paymentId,
                true,
                true
        );

        when(factory.get(PaymentProviderType.STRIPE)).thenReturn(provider);
        when(provider.handleWebhook(any(), any())).thenReturn(webhookResult);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        // Act
        paymentWebhookService.handleStripeWebhook("payload", "signature");

        // Assert
        verify(paymentRepository).save(payment);
    }

    @Test
    void handleStripeWebhook_doesNothing_whenWebhookIsNotRelevant() {
        // Arrange
        WebhookResult webhookResult = new WebhookResult(
                UUID.randomUUID(),
                false,
                false

        );

        when(factory.get(PaymentProviderType.STRIPE))
                .thenReturn(provider);

        when(provider.handleWebhook(any(), any()))
                .thenReturn(webhookResult);

        // Act
        paymentWebhookService.handleStripeWebhook("payload", "signature");

        // Assert
        verify(paymentRepository, never()).findById(any());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void handleStripeWebhook_doesNothing_whenPaymentIdIsNull() {
        // Arrange
        WebhookResult webhookResult = new WebhookResult(
                null,
                true,
                true
        );

        when(factory.get(PaymentProviderType.STRIPE)).thenReturn(provider);
        when(provider.handleWebhook(any(), any())).thenReturn(webhookResult);

        // Act
        paymentWebhookService.handleStripeWebhook("payload", "signature");

        // Assert
        verify(paymentRepository, never()).save(any());
        verify(paymentRepository, never()).findById(any());
    }

    @Test
    void handleStripeWebhook_throwsNotFound_whenPaymentDoesNotExist() {
        // Arrange
        UUID paymentId = UUID.randomUUID();

        WebhookResult webhookResult = new WebhookResult(
                paymentId,
                true,
                true
        );

        when(factory.get(PaymentProviderType.STRIPE)).thenReturn(provider);
        when(provider.handleWebhook(any(), any())).thenReturn(webhookResult);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() ->
                paymentWebhookService.handleStripeWebhook("payload", "signature")
        ).isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Payment not found");
    }
}
