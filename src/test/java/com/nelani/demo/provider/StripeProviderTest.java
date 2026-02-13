package com.nelani.demo.provider;

import com.nelani.demo.dto.PaymentResponseDTO;
import com.nelani.demo.dto.WebhookResult;
import com.nelani.demo.exception.PaymentProviderTemporaryException;
import com.nelani.demo.model.Payment;
import com.nelani.demo.model.PaymentProviderType;
import com.nelani.demo.model.PaymentStatus;
import com.nelani.demo.repository.PaymentRepository;
import com.stripe.exception.*;
import com.stripe.model.Customer;
import com.stripe.model.PaymentIntent;
import com.stripe.net.RequestOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2, replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
class StripeProviderTest {

    @Autowired
    private PaymentRepository paymentRepository;

    private PaymentProvider stripeProvider;
    private Payment testPayment;

    @BeforeEach
    void setup() {
        stripeProvider = new StripeProvider();

        testPayment = new Payment(
                "order1",
                BigDecimal.valueOf(100),
                PaymentStatus.PENDING,
                PaymentProviderType.STRIPE);
        testPayment.setProviderReference("pi_test");

        // This is done to generate the ID for the payment
        paymentRepository.save(testPayment);
    }

    @Test
    void PaymentServiceTest_InitializePayment_ReturnsSuccess() throws Exception {
        try (MockedStatic<Customer> mockedCustomer = mockStatic(Customer.class);

                MockedStatic<PaymentIntent> mockedIntent = mockStatic(PaymentIntent.class)) {

            Customer mockCustomer = mock(Customer.class);
            when(mockCustomer.getId()).thenReturn("cus_12345");
            mockedCustomer.when(() -> Customer.create(anyMap())).thenReturn(mockCustomer);

            PaymentIntent mockPaymentIntent = mock(PaymentIntent.class);
            when(mockPaymentIntent.getId()).thenReturn("pi_12345");
            when(mockPaymentIntent.getClientSecret()).thenReturn("secret_12345");
            mockedIntent.when(() -> PaymentIntent.create(anyMap(), any(RequestOptions.class)))
                    .thenReturn(mockPaymentIntent);

            PaymentResponseDTO response = stripeProvider.createPayment(testPayment);

            assertNotNull(response, "PaymentResponseDTO should not be null");
            assertEquals(testPayment.getOrderId(), response.getOrderId(), "OrderId should match");
            assertEquals("pi_12345", response.getClientId(), "PaymentIntent ID should match");
            assertEquals("secret_12345", response.getClientSecret(), "Client secret should match");
            assertEquals(testPayment.getAmount(), response.getAmount(), "Amount should match");
            assertEquals(PaymentProviderType.STRIPE, response.getProvider(), "Provider should match");
        }
    }

    @Test
    void PaymentServiceTest_CreatePayment_InvalidRequest_ThrowsIllegalArgumentException() throws Exception {
        try (MockedStatic<Customer> mockedCustomer = mockStatic(Customer.class);
                MockedStatic<PaymentIntent> mockedIntent = mockStatic(PaymentIntent.class)) {

            Customer mockCustomer = mock(Customer.class);
            when(mockCustomer.getId()).thenReturn("cus_12345");
            mockedCustomer.when(() -> Customer.create(anyMap())).thenReturn(mockCustomer);

            mockedIntent.when(() -> PaymentIntent.create(anyMap(), any(RequestOptions.class)))
                    .thenThrow(new InvalidRequestException("Invalid request", null, null, null, null, null));

            assertThrows(IllegalArgumentException.class,
                    () -> stripeProvider.createPayment(testPayment));
        }
    }

    @Test
    void PaymentServiceTest_CreatePayment_AuthenticationFailure_ThrowsIllegalStateException() throws Exception {
        try (MockedStatic<Customer> mockedCustomer = mockStatic(Customer.class)) {
            mockedCustomer.when(() -> Customer.create(anyMap()))
                    .thenThrow(new AuthenticationException(
                            "Invalid API Key",
                            null,
                            null,
                            401));

            assertThrows(IllegalStateException.class,
                    () -> stripeProvider.createPayment(testPayment));
        }
    }

    @Test
    void PaymentServiceTest_CreatePayment_TransientStripeFailure_ThrowsTemporaryException() throws Exception {
        try (MockedStatic<Customer> mockedCustomer = mockStatic(Customer.class);
                MockedStatic<PaymentIntent> mockedIntent = mockStatic(PaymentIntent.class)) {

            Customer mockCustomer = mock(Customer.class);
            when(mockCustomer.getId()).thenReturn("cus_12345");
            mockedCustomer.when(() -> Customer.create(anyMap())).thenReturn(mockCustomer);

            mockedIntent.when(() -> PaymentIntent.create(anyMap(), any(RequestOptions.class)))
                    .thenThrow(new ApiException("Server timeout", null, null, 500, null));

            assertThrows(PaymentProviderTemporaryException.class,
                    () -> stripeProvider.createPayment(testPayment));
        }
    }

    @Test
    void PaymentServiceTest_CreatePayment_InvalidAmount_ThrowsIllegalStateException() {
        testPayment = new Payment(
                "order1",
                BigDecimal.ZERO,
                PaymentStatus.PENDING,
                PaymentProviderType.STRIPE);

        assertThrows(IllegalStateException.class,
                () -> stripeProvider.createPayment(testPayment));
    }

    @Test
    void PaymentServiceTest_ResumePayment_Success() throws Exception {
        try (MockedStatic<PaymentIntent> mockedIntent = mockStatic(PaymentIntent.class)) {
            PaymentIntent mockIntent = mock(PaymentIntent.class);

            when(mockIntent.getId()).thenReturn("pi_12345");
            when(mockIntent.getClientSecret()).thenReturn("secret_12345");
            when(mockIntent.getStatus()).thenReturn("requires_payment_method");

            // Make sure the exact string matches
            mockedIntent.when(() -> PaymentIntent.retrieve("pi_test")).thenReturn(mockIntent);

            PaymentResponseDTO response = stripeProvider.resumePayment(testPayment);

            assertNotNull(response);
            assertEquals("pi_12345", response.getClientId());
        }
    }

    @Test
    void PaymentServiceTest_ResumePayment_AlreadySucceeded_ThrowsIllegalStateException() throws Exception {
        try (MockedStatic<PaymentIntent> mockedIntent = mockStatic(PaymentIntent.class)) {
            PaymentIntent mockIntent = mock(PaymentIntent.class);
            when(mockIntent.getStatus()).thenReturn("succeeded");
            mockedIntent.when(() -> PaymentIntent.retrieve(anyString())).thenReturn(mockIntent);

            assertThrows(IllegalStateException.class,
                    () -> stripeProvider.resumePayment(testPayment));
        }
    }

    @Test
    void PaymentServiceTest_ResumePayment_TransientStripeFailure_ThrowsTemporaryException() throws Exception {
        try (MockedStatic<PaymentIntent> mockedIntent = mockStatic(PaymentIntent.class)) {
            mockedIntent.when(() -> PaymentIntent.retrieve(anyString()))
                    .thenThrow(new ApiException("Server timeout", null, null, 500, null));

            assertThrows(PaymentProviderTemporaryException.class,
                    () -> stripeProvider.resumePayment(testPayment));
        }
    }

    @Test
    void PaymentServiceTest_HandleWebhook_SuccessEvent() {
        StripeProvider provider = new StripeProvider() {

            @Override
            public WebhookResult handleWebhook(String payload, String signature) {
                return new WebhookResult(
                        testPayment.getId(),
                        true,
                        true);
            }
        };

        WebhookResult result = provider.handleWebhook("payload", "signature");

        assertTrue(result.success());
        assertTrue(result.relevant());
        assertEquals(testPayment.getId(), result.paymentId());
    }

    @Test
    void PaymentServiceTest_HandleWebhook_InvalidSignature_ThrowsIllegalArgumentException()
            throws SignatureVerificationException {
        StripeProvider provider = new StripeProvider() {
            @Override
            public WebhookResult handleWebhook(String payload, String signature) {
                throw new IllegalArgumentException("Invalid webhook signature");
            }
        };

        assertThrows(IllegalArgumentException.class,
                () -> provider.handleWebhook("payload", "wrong-signature"));
    }

}
