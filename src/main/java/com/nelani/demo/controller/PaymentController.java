package com.nelani.demo.controller;

import com.nelani.demo.dto.PaymentRequestDTO;
import com.nelani.demo.dto.PaymentResponseDTO;
import com.nelani.demo.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

        private final PaymentService paymentService;

        public PaymentController(PaymentService paymentService) {
                this.paymentService = paymentService;
        }

        @Operation(summary = "Initialize a payment", description = "Creates a new payment request and initializes it with the selected payment provider.")
        @ApiResponse(responseCode = "200", description = "Payment successfully initialized", content = @Content(schema = @Schema(implementation = PaymentResponseDTO.class)))
        @PostMapping
        public ResponseEntity<PaymentResponseDTO> initializePayment(
                        @Valid @RequestBody PaymentRequestDTO request) {
                var result = paymentService.initializePayment(request);
                return ResponseEntity.ok(result);
        }
}
