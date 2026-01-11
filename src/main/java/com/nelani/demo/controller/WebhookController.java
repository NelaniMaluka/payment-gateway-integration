package com.nelani.demo.controller;

import com.nelani.demo.service.PaymentWebhookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhooks")
@Tag(name = "Webhooks", description = "Internal endpoints for handling payment provider webhooks")
public class WebhookController {

        private final PaymentWebhookService paymentWebhookService;

        public WebhookController(PaymentWebhookService paymentWebhookService) {
                this.paymentWebhookService = paymentWebhookService;
        }

        @Operation(summary = "Handle Stripe webhook events", description = """
                        Receives and processes webhook events sent by Stripe.
                        The request signature is verified to ensure authenticity.
                        This endpoint is intended for internal provider communication only.
                        """)
        @ApiResponse(responseCode = "200", description = "Webhook event processed successfully")
        @PostMapping("/stripe")
        public ResponseEntity<Void> handleWebhook(

                        @Parameter(description = "Raw webhook payload sent by Stripe", required = true) @RequestBody String payload,

                        @Parameter(description = "Stripe webhook signature used to verify request authenticity", required = true, in = ParameterIn.HEADER, name = "Stripe-Signature") @RequestHeader("Stripe-Signature") String signature) {
                paymentWebhookService.handleStripeWebhook(payload, signature);
                return ResponseEntity.ok().build();
        }
}
