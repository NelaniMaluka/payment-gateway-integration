package com.nelani.demo.provider;

import com.nelani.demo.dto.WebhookResult;
import com.nelani.demo.model.Payment;
import com.nelani.demo.model.PaymentProviderType;
import com.nelani.demo.repository.PaymentRepository;
import com.nelani.demo.service.PaymentProviderFactory;
import com.nelani.demo.service.impl.PaymentWebhookServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentWebhookServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentProviderFactory paymentProviderFactory;

    @Mock
    private PaymentProvider paymentProvider;

    @InjectMocks
    private PaymentWebhookServiceImpl paymentWebhookService;

    private Payment payment;

    @BeforeEach
    void setUp() {
        payment = mock(Payment.class);

        when(paymentProviderFactory.get(PaymentProviderType.STRIPE))
                .thenReturn(paymentProvider);
    }

    @Test
    void handleStripeWebhook_doesNothing_whenWebhookIsNotRelevant() {
        // Arrange
        WebhookResult webhookResult = new WebhookResult(
                null,
                false,
                false
        );

        when(paymentProvider.handleWebhook(anyString(), anyString()))
                .thenReturn(webhookResult);

        // Act
        paymentWebhookService.handleStripeWebhook("payload", "signature");

        // Assert
        verifyNoInteractions(paymentRepository);
    }

    @Test
    void handleStripeWebhook_doesNothing_whenPaymentIdIsNull() {
        // Arrange
        WebhookResult webhookResult = new WebhookResult(
                null,
                true,
                true
        );

        when(paymentProvider.handleWebhook(anyString(), anyString()))
                .thenReturn(webhookResult);

        // Act
        paymentWebhookService.handleStripeWebhook("payload", "signature");

        // Assert
        verifyNoInteractions(paymentRepository);
    }

    @Test
    void handleStripeWebhook_throwsNotFound_whenPaymentDoesNotExist() {
        // Arrange
        WebhookResult webhookResult = new WebhookResult(
                UUID.randomUUID(),
                true,
                true
        );

        when(paymentProvider.handleWebhook(anyString(), anyString()))
                .thenReturn(webhookResult);

        when(paymentRepository.findById(webhookResult.paymentId()))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResponseStatusException.class, () ->
                paymentWebhookService.handleStripeWebhook("payload", "signature")
        );
    }

    @Test
    void handleStripeWebhook_marksPaymentSuccess_whenWebhookSuccessful() {
        // Arrange
        WebhookResult webhookResult = new WebhookResult(
                UUID.randomUUID(),
                true,
                true
        );

        when(paymentProvider.handleWebhook(anyString(), anyString()))
                .thenReturn(webhookResult);

        when(paymentRepository.findById(webhookResult.paymentId()))
                .thenReturn(Optional.of(payment));

        // Act
        paymentWebhookService.handleStripeWebhook("payload", "signature");

        // Assert
        verify(payment).markSuccess();
        verify(payment, never()).markFailed();
        verify(paymentRepository).save(payment);
    }

    @Test
    void handleStripeWebhook_marksPaymentFailed_whenWebhookNotSuccessful() {
        // Arrange
        WebhookResult webhookResult = new WebhookResult(
                UUID.randomUUID(),
                false,
                true
        );

        when(paymentProvider.handleWebhook(anyString(), anyString()))
                .thenReturn(webhookResult);

        when(paymentRepository.findById(webhookResult.paymentId()))
                .thenReturn(Optional.of(payment));

        // Act
        paymentWebhookService.handleStripeWebhook("payload", "signature");

        // Assert
        verify(payment).markFailed();
        verify(payment, never()).markSuccess();
        verify(paymentRepository).save(payment);
    }
}
