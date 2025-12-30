package com.nelani.demo.dto;

import com.nelani.demo.model.PaymentProviderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PaymentResponseDTO(
                @NotBlank(message = "Payment ID is required") String paymentId,

                @NotNull(message = "Provider is required") PaymentProviderType provider,

                @NotBlank(message = "Status is required") String status) {
}
