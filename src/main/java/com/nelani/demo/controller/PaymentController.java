package com.nelani.demo.controller;

import com.nelani.demo.dto.PaymentRequestDTO;
import com.nelani.demo.dto.PaymentResponseDTO;
import com.nelani.demo.model.PaymentSortField;
import com.nelani.demo.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
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

        @Operation(summary = "Get all payments", description = "Returns a paginated and sorted list of payments.")
        @ApiResponse(responseCode = "200", description = "Payments retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PaymentResponseDTO.class)))
        @GetMapping
        public ResponseEntity<Page<PaymentResponseDTO>> getAllPayments(

                        @Parameter(description = "Field to sort by", schema = @Schema(implementation = PaymentSortField.class, defaultValue = "CREATED_AT")) @RequestParam(defaultValue = "CREATED_AT") PaymentSortField sortBy,

                        @Parameter(description = "Sort direction", schema = @Schema(implementation = Sort.Direction.class, defaultValue = "DESC")) @RequestParam(defaultValue = "DESC") Sort.Direction direction,

                        @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,

                        @Parameter(description = "Number of records per page", example = "10") @RequestParam(defaultValue = "10") int size

        ) {
                var result = paymentService.getAllPayments(sortBy, direction, page, size);
                return ResponseEntity.ok(result);
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
