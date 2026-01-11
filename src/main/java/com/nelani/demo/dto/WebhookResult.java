package com.nelani.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Result of processing a payment provider webhook event")
public record WebhookResult(

                @Schema(description = "Internal payment identifier extracted from provider metadata. " +
                                "Null if the webhook event is not relevant or could not be mapped.", example = "550e8400-e29b-41d4-a716-446655440000", nullable = true) UUID paymentId,

                @Schema(description = "Indicates whether the payment operation succeeded. " +
                                "False for failed or cancelled payments.", example = "true") boolean success,

                @Schema(description = "Indicates whether the webhook event was relevant and processed by the system. " +
                                "False for ignored or unsupported event types.", example = "true") boolean relevant) {
}
