package com.nelani.demo.controller;

import com.nelani.demo.service.PaymentWebhookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WebhookController.class)
@ActiveProfiles("test")
class WebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentWebhookService paymentWebhookService;

    @Test
    void WebhookControllerTest_handleStripeWebhook_returnsOk() throws Exception {
        // Arrange
        String payload = "{ \"type\": \"payment_intent.succeeded\" }";
        String signature = "t=123456,v1=abc123";

        doNothing().when(paymentWebhookService)
                .handleStripeWebhook(payload, signature);

        // Act & Assert
        mockMvc.perform(post("/webhooks/stripe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Stripe-Signature", signature)
                        .content(payload))
                .andExpect(status().isOk());

        // Verify
        verify(paymentWebhookService)
                .handleStripeWebhook(payload, signature);
    }
}

