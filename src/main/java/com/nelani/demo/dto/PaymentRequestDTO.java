package com.nelani.demo.dto;

import com.nelani.demo.model.PaymentProviderType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Request payload used to initialize a payment")
public record PaymentRequestDTO(

                @Schema(description = "Client-side or system order identifier associated with this payment", example = "ORD-2025-0001") @NotBlank(message = "Order ID is required") String orderId,

                @Schema(description = "Amount to be charged for the payment", example = "150.00", minimum = "0.01") @NotNull(message = "Amount is required") @DecimalMin(value = "0.01", message = "Amount must be greater than zero") BigDecimal amount,

                @Schema(description = "Payment provider to process the transaction", example = "PAYSTACK") @NotNull(message = "Provider is required") PaymentProviderType provider) {
}
