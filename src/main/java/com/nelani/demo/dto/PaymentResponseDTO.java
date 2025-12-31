package com.nelani.demo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nelani.demo.model.PaymentProviderType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Response returned after a payment is initialized")
public record PaymentResponseDTO(

                @Schema(description = "Unique identifier of the payment", example = "pay_9f8a7b6c") @NotBlank(message = "Payment ID is required") String paymentId,

                @Schema(description = "Payment provider used for the transaction", example = "PAYSTACK") @NotNull(message = "Provider is required") PaymentProviderType provider,

                @Schema(description = "Current status of the payment", example = "PENDING") @NotBlank(message = "Status is required") String status,

                @Schema(description = "Timestamp when the payment was created", example = "2025-01-06T10:15:30+02:00") @NotNull OffsetDateTime createdAt,

                @Schema(description = "Timestamp when the payment was expires", example = "2025-01-06T10:15:30+02:00") @NotNull OffsetDateTime expiresAt,

                @Schema(description = "Timestamp when the payment was successfully completed. Null if not paid yet.", example = "2025-01-06T10:16:42+02:00", nullable = true) OffsetDateTime completedAt

) {
}
