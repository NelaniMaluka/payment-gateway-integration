package com.nelani.demo.service;

import com.nelani.demo.dto.PaymentRequestDTO;
import com.nelani.demo.dto.PaymentResponseDTO;
import com.nelani.demo.model.Payment;
import com.nelani.demo.model.PaymentProviderType;
import com.nelani.demo.model.PaymentSortField;
import com.nelani.demo.model.PaymentStatus;
import com.nelani.demo.provider.PaymentProvider;
import com.nelani.demo.repository.PaymentRepository;
import com.nelani.demo.service.impl.PaymentServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class PaymentServiceTest {

        @Mock
        private PaymentRepository paymentRepository;

        @Mock
        private PaymentProviderFactory factory;

        @Mock
        private PaymentProvider provider;

        @InjectMocks
        private PaymentServiceImpl paymentService;

        @Test
        public void PaymentServiceTest_GetAllPayments_ReturnsPaymentResponseDTOPage() {
                // Arrange
                final Payment payment = new Payment(
                                "order1",
                                BigDecimal.valueOf(100L),
                                PaymentStatus.PENDING,
                                PaymentProviderType.PAYPAL,
                                OffsetDateTime.now().plusDays(1));
                final Payment payment1 = new Payment(
                                "order2",
                                BigDecimal.valueOf(10L),
                                PaymentStatus.PENDING,
                                PaymentProviderType.STRIPE,
                                OffsetDateTime.now().plusDays(1));
                final Page<Payment> resultsList = new PageImpl<>(List.of(payment1, payment), PageRequest.of(0, 10), 1);

                // Stub
                when(paymentRepository.findAll((Pageable) any()))
                                .thenReturn(resultsList);

                // Act & Assert
                var results = paymentService.getAllPayments(PaymentSortField.AMOUNT, Sort.Direction.DESC, 0, 10);
                Assertions.assertThat(results.getTotalElements()).isEqualTo(resultsList.getTotalElements());
                Assertions.assertThat(results.stream().findFirst().get().amount()).isEqualTo(payment1.getAmount());
                Assertions.assertThat(results).extracting(PaymentResponseDTO::orderId).contains(payment.getOrderId(),
                                payment1.getOrderId());
                Assertions.assertThat(results).extracting(PaymentResponseDTO::provider).contains(payment.getProvider(),
                                payment1.getProvider());
                Assertions.assertThat(results).extracting(PaymentResponseDTO::createdAt)
                                .contains(payment.getCreatedAt(), payment1.getCreatedAt());
                Assertions.assertThat(results).extracting(PaymentResponseDTO::expiresAt)
                                .contains(payment.getExpiresAt(), payment1.getExpiresAt());
        }

        @Test
        public void PaymentServiceTest_InitializePayment_ReturnsPendingError() {
                // Arrange
                final Payment payment = new Payment(
                                "order1",
                                BigDecimal.valueOf(100L),
                                PaymentStatus.PENDING,
                                PaymentProviderType.PAYPAL,
                                OffsetDateTime.now().plusDays(1));
                final PaymentRequestDTO request = new PaymentRequestDTO(payment.getOrderId(), payment.getAmount(),
                                payment.getProvider());

                // Stub
                when(paymentRepository.findByOrderId(anyString())).thenReturn(Optional.of(payment));

                // Assert
                assertThatThrownBy(() -> paymentService.initializePayment(request))
                                .isInstanceOf(ResponseStatusException.class)
                                .hasMessageContaining("An active payment already exists for this order.");
        }

        @Test
        public void PaymentServiceTest_InitializePayment_ReturnsSuccessError() {
                // Arrange
                final Payment payment = new Payment(
                                "order1",
                                BigDecimal.valueOf(100L),
                                PaymentStatus.SUCCESS,
                                PaymentProviderType.PAYPAL,
                                OffsetDateTime
                                                .now().plusDays(1));
                final PaymentRequestDTO request = new PaymentRequestDTO(payment.getOrderId(), payment.getAmount(),
                                payment.getProvider());

                // Stub
                when(paymentRepository.findByOrderId(anyString())).thenReturn(Optional.of(payment));

                // Assert
                assertThatThrownBy(() -> paymentService.initializePayment(request))
                                .isInstanceOf(ResponseStatusException.class)
                                .hasMessageContaining("Order already paid.");
        }

        @Test
        public void PaymentServiceTest_InitializePayment_ReturnsExpiredError() {
                // Arrange
                final Payment payment = new Payment(
                                "order1",
                                BigDecimal.valueOf(100L),
                                PaymentStatus.EXPIRED,
                                PaymentProviderType.STRIPE,
                                OffsetDateTime
                                                .now().plusDays(1));
                final PaymentRequestDTO request = new PaymentRequestDTO(payment.getOrderId(), payment.getAmount(),
                                payment.getProvider());

                // Stub
                when(paymentRepository.findByOrderId(anyString())).thenReturn(Optional.of(payment));

                // Assert
                assertThatThrownBy(() -> paymentService.initializePayment(request))
                                .isInstanceOf(ResponseStatusException.class)
                                .hasMessageContaining("Order already expired.");
        }

        @Test
        public void PaymentServiceTest_InitializePayment_ReturnsSuccess() {
                // Arrange
                final Payment payment = new Payment(
                                "order1",
                                BigDecimal.valueOf(100L),
                                PaymentStatus.FAILED,
                                PaymentProviderType.PAYPAL,
                                OffsetDateTime
                                                .now().plusDays(1));
                final PaymentRequestDTO request = new PaymentRequestDTO(payment.getOrderId(), payment.getAmount(),
                                payment.getProvider());

                // Stub
                when(paymentRepository.findByOrderId(anyString()))
                                .thenReturn(Optional.of(payment));
                when(factory.get(any(PaymentProviderType.class)))
                                .thenReturn(provider);
                when(provider.createPayment(any(Payment.class)))
                                .thenReturn(null);

                // Assert
                var result = paymentService.initializePayment(request);
                Assertions.assertThat(result).isNull();
        }

        @Test
        public void PaymentServiceTest_InitializePayment_ReturnsSuccess_WhenFailed() {
                // Arrange
                final Payment payment = new Payment(
                                "order1",
                                BigDecimal.valueOf(100L),
                                PaymentStatus.FAILED,
                                PaymentProviderType.PAYPAL,
                                OffsetDateTime
                                                .now().plusDays(1));
                final PaymentRequestDTO request = new PaymentRequestDTO(payment.getOrderId(), payment.getAmount(),
                                payment.getProvider());

                // Stub
                when(paymentRepository.findByOrderId(anyString()))
                                .thenReturn(Optional.of(payment));
                when(factory.get(any(PaymentProviderType.class)))
                                .thenReturn(provider);
                when(provider.createPayment(any(Payment.class)))
                                .thenReturn(null);

                // Assert
                var result = paymentService.initializePayment(request);
                Assertions.assertThat(result).isNull();
        }

        @Test
        public void PaymentServiceTest_InitializePayment_ReturnsSuccess_WhenInitiating() {
                // Arrange
                final Payment payment = new Payment(
                                "order1",
                                BigDecimal.valueOf(100L),
                                PaymentStatus.INITIATING,
                                PaymentProviderType.STRIPE,
                                OffsetDateTime
                                                .now().plusDays(1));
                final PaymentRequestDTO request = new PaymentRequestDTO(payment.getOrderId(), payment.getAmount(),
                                payment.getProvider());

                // Stub
                when(paymentRepository.findByOrderId(anyString()))
                                .thenReturn(Optional.of(payment));
                when(factory.get(any(PaymentProviderType.class)))
                                .thenReturn(provider);
                when(provider.createPayment(any(Payment.class)))
                                .thenReturn(null);

                // Assert
                var result = paymentService.initializePayment(request);
                Assertions.assertThat(result).isNull();
        }

}
