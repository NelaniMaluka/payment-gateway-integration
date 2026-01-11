package com.nelani.demo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nelani.demo.model.PaymentProviderType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Response returned after a payment is initialized")
public class PaymentResponseDTO {

        @Schema(description = "Unique identifier of the payment", example = "pay_9f8a7b6c")
        @NotBlank(message = "Order ID is required")
        String orderId;

        @Schema(description = "Provider-generated payment reference or intent ID", example = "pi_3NQxYkLkdIwHu7ix0X1")
        String clientId;

        @Schema(description = "Client secret used by the frontend to complete the payment with the provider SDK", example = "pi_3NQxYkLkdIwHu7ix0X1_secret_abc123", accessMode = Schema.AccessMode.READ_ONLY)
        String clientSecret;

        @Schema(description = "Payment amount", example = "199.99")
        @NotNull(message = "Amount is required")
        BigDecimal amount;

        @Schema(description = "Payment provider used for the transaction", example = "PAYSTACK")
        @NotNull(message = "Provider is required")
        PaymentProviderType provider;

        @Schema(description = "Current status of the payment", example = "PENDING")
        @NotBlank(message = "Status is required")
        String status;

        @Schema(description = "Timestamp when the payment was created", example = "2025-01-06T10:15:30+02:00")
        @NotNull
        OffsetDateTime createdAt;

        @Schema(description = "Timestamp when the payment was expires", example = "2025-01-06T10:15:30+02:00")
        @NotNull
        OffsetDateTime expiresAt;

        @Schema(description = "Timestamp when the payment was successfully completed. Null if not paid yet.", example = "2025-01-06T10:16:42+02:00", nullable = true)
        OffsetDateTime completedAt;

}
