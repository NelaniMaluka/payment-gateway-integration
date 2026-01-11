package com.nelani.demo.repository;

import com.nelani.demo.model.Payment;
import com.nelani.demo.model.PaymentProviderType;
import com.nelani.demo.model.PaymentSortField;
import com.nelani.demo.model.PaymentStatus;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2, replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
public class PaymentRepositoryTest {

        @Autowired
        private PaymentRepository paymentRepository;

        @Test
        public void PaymentServiceTest_FindAll_ReturnsPaymentsPage_OrderedByAmount() {
                // Arrange
                OffsetDateTime now = OffsetDateTime.now();
                final Payment payment1 = new Payment(
                                "order1",
                                BigDecimal.valueOf(100L),
                                PaymentStatus.PENDING,
                                PaymentProviderType.PAYPAL);
                final Payment payment2 = new Payment(
                                "order2",
                                BigDecimal.valueOf(10L),
                                PaymentStatus.PENDING,
                                PaymentProviderType.STRIPE);

                paymentRepository.saveAll(List.of(payment1, payment2));

                Pageable pageable = PageRequest.of(0, 10,
                                Sort.by(Sort.Direction.ASC, PaymentSortField.AMOUNT.fieldName()));

                // Act
                var resultsPage = paymentRepository.findAll(pageable);
                List<Payment> resultsList = resultsPage.getContent();

                // Assert
                Assertions.assertThat(resultsPage.getTotalElements()).isEqualTo(2);

                // Check full list is sorted correctly by amount
                Assertions.assertThat(resultsList).extracting(Payment::getAmount)
                                .containsExactly(payment2.getAmount(), payment1.getAmount());

                // Check orderIds, providers, expiresAt
                Assertions.assertThat(resultsList).extracting(Payment::getOrderId)
                                .containsExactly(payment2.getOrderId(), payment1.getOrderId());
                Assertions.assertThat(resultsList).extracting(Payment::getProvider)
                                .containsExactly(payment2.getProvider(), payment1.getProvider());
                Assertions.assertThat(resultsList).extracting(Payment::getExpiresAt)
                                .containsExactly(payment2.getExpiresAt(), payment1.getExpiresAt());
        }

        @Test
        public void PaymentServiceTest_FindAll_ReturnsPaymentsPage_OrderedByStatus() {
                // Arrange
                OffsetDateTime now = OffsetDateTime.now();
                final Payment payment1 = new Payment(
                                "order1",
                                BigDecimal.valueOf(100L),
                                PaymentStatus.INITIATING,
                                PaymentProviderType.PAYPAL);
                final Payment payment2 = new Payment(
                                "order2",
                                BigDecimal.valueOf(10L),
                                PaymentStatus.PENDING,
                                PaymentProviderType.STRIPE);

                paymentRepository.saveAll(List.of(payment1, payment2));

                Pageable pageable = PageRequest.of(0, 10,
                                Sort.by(Sort.Direction.DESC, PaymentSortField.STATUS.fieldName()));

                // Act
                var resultsPage = paymentRepository.findAll(pageable);
                List<Payment> resultsList = resultsPage.getContent();

                // Assert
                Assertions.assertThat(resultsPage.getTotalElements()).isEqualTo(2);

                // Check full list is sorted correctly by amount
                Assertions.assertThat(resultsList).extracting(Payment::getStatus)
                                .containsExactly(payment2.getStatus(), payment1.getStatus());

                // Check orderIds, providers, expiresAt
                Assertions.assertThat(resultsList).extracting(Payment::getOrderId)
                                .containsExactly(payment2.getOrderId(), payment1.getOrderId());
                Assertions.assertThat(resultsList).extracting(Payment::getProvider)
                                .containsExactly(payment2.getProvider(), payment1.getProvider());
                Assertions.assertThat(resultsList).extracting(Payment::getExpiresAt)
                                .containsExactly(payment2.getExpiresAt(), payment1.getExpiresAt());
        }

        @Test
        public void PaymentServiceTest_FindAll_ReturnsPaymentsPage_OrderedByCreatedAt() {
                // Arrange
                OffsetDateTime now = OffsetDateTime.now();
                final Payment payment1 = new Payment(
                                "order1",
                                BigDecimal.valueOf(100L),
                                PaymentStatus.INITIATING,
                                PaymentProviderType.PAYPAL);
                final Payment payment2 = new Payment(
                                "order2",
                                BigDecimal.valueOf(10L),
                                PaymentStatus.PENDING,
                                PaymentProviderType.STRIPE);

                paymentRepository.saveAll(List.of(payment1, payment2));

                Pageable pageable = PageRequest.of(0, 10,
                                Sort.by(Sort.Direction.ASC, PaymentSortField.CREATED_AT.fieldName()));

                // Act
                var resultsPage = paymentRepository.findAll(pageable);
                List<Payment> resultsList = resultsPage.getContent();

                // Assert
                Assertions.assertThat(resultsPage.getTotalElements()).isEqualTo(2);

                // Check full list is sorted correctly by amount
                Assertions.assertThat(resultsList).extracting(Payment::getStatus)
                                .containsExactly(payment1.getStatus(), payment2.getStatus());

                // Check orderIds, providers, expiresAt
                Assertions.assertThat(resultsList).extracting(Payment::getOrderId)
                                .containsExactly(payment1.getOrderId(), payment2.getOrderId());
                Assertions.assertThat(resultsList).extracting(Payment::getProvider)
                                .containsExactly(payment1.getProvider(), payment2.getProvider());
                Assertions.assertThat(resultsList).extracting(Payment::getExpiresAt)
                                .containsExactly(payment1.getExpiresAt(), payment2.getExpiresAt());
        }

        @Test
        public void PaymentServiceTest_FindAll_ReturnsPaymentsPage_OrderedByExpiresAt() {
                // Arrange
                OffsetDateTime now = OffsetDateTime.now();
                final Payment payment1 = new Payment(
                                "order1",
                                BigDecimal.valueOf(100L),
                                PaymentStatus.INITIATING,
                                PaymentProviderType.PAYPAL);
                final Payment payment2 = new Payment(
                                "order2",
                                BigDecimal.valueOf(10L),
                                PaymentStatus.PENDING,
                                PaymentProviderType.STRIPE);

                paymentRepository.saveAll(List.of(payment1, payment2));

                Pageable pageable = PageRequest.of(0, 10,
                                Sort.by(Sort.Direction.DESC, PaymentSortField.EXPIRES_AT.fieldName()));

                // Act
                var resultsPage = paymentRepository.findAll(pageable);
                List<Payment> resultsList = resultsPage.getContent();

                // Assert
                Assertions.assertThat(resultsPage.getTotalElements()).isEqualTo(2);

                // Check full list is sorted correctly by amount
                Assertions.assertThat(resultsList).extracting(Payment::getStatus)
                                .containsExactlyInAnyOrder(payment1.getStatus(), payment2.getStatus());

                // Check orderIds, providers, expiresAt
                Assertions.assertThat(resultsList).extracting(Payment::getOrderId)
                                .containsExactly(payment1.getOrderId(), payment2.getOrderId());
                Assertions.assertThat(resultsList).extracting(Payment::getProvider)
                                .containsExactly(payment1.getProvider(), payment2.getProvider());
                Assertions.assertThat(resultsList).extracting(Payment::getExpiresAt)
                                .containsExactly(payment1.getExpiresAt(), payment2.getExpiresAt());
        }

        @Test
        public void PaymentServiceTest_FindAll_ReturnsPaymentsPage_OrderedByProvider() {
                // Arrange
                OffsetDateTime now = OffsetDateTime.now();
                final Payment payment1 = new Payment(
                                "order1",
                                BigDecimal.valueOf(100L),
                                PaymentStatus.INITIATING,
                                PaymentProviderType.PAYPAL);
                final Payment payment2 = new Payment(
                                "order2",
                                BigDecimal.valueOf(10L),
                                PaymentStatus.PENDING,
                                PaymentProviderType.STRIPE);

                paymentRepository.saveAll(List.of(payment1, payment2));

                Pageable pageable = PageRequest.of(0, 10,
                                Sort.by(Sort.Direction.ASC, PaymentSortField.PROVIDER.fieldName()));

                // Act
                var resultsPage = paymentRepository.findAll(pageable);
                List<Payment> resultsList = resultsPage.getContent();

                // Assert
                Assertions.assertThat(resultsPage.getTotalElements()).isEqualTo(2);

                // Check full list is sorted correctly by amount
                Assertions.assertThat(resultsList).extracting(Payment::getStatus)
                                .containsExactly(payment1.getStatus(), payment2.getStatus());

                // Check orderIds, providers, expiresAt
                Assertions.assertThat(resultsList).extracting(Payment::getOrderId)
                                .containsExactly(payment1.getOrderId(), payment2.getOrderId());
                Assertions.assertThat(resultsList).extracting(Payment::getProvider)
                                .containsExactly(payment1.getProvider(), payment2.getProvider());
                Assertions.assertThat(resultsList).extracting(Payment::getExpiresAt)
                                .containsExactly(payment1.getExpiresAt(), payment2.getExpiresAt());
        }

        @Test
        public void PaymentServiceTest_FindByOrderId_ReturnsPayment() {
                // Arrange
                OffsetDateTime now = OffsetDateTime.now();
                final Payment payment1 = new Payment(
                                "order1",
                                BigDecimal.valueOf(100L),
                                PaymentStatus.INITIATING,
                                PaymentProviderType.PAYPAL);
                final Payment payment2 = new Payment(
                                "order2",
                                BigDecimal.valueOf(10L),
                                PaymentStatus.PENDING,
                                PaymentProviderType.STRIPE);

                paymentRepository.saveAll(List.of(payment1, payment2));

                // Act & Assert
                var result = paymentRepository.findByOrderId(payment2.getOrderId());
                Assertions.assertThat(result).isNotNull();
                Assertions.assertThat(result.get().getOrderId()).isEqualTo(payment2.getOrderId());
                Assertions.assertThat(result.get().getAmount()).isEqualTo(payment2.getAmount());
                Assertions.assertThat(result.get().getProvider()).isEqualTo(payment2.getProvider());
                Assertions.assertThat(result.get().getCreatedAt()).isEqualTo(payment2.getCreatedAt());
                Assertions.assertThat(result.get().getStatus()).isEqualTo(payment2.getStatus());
        }

}
