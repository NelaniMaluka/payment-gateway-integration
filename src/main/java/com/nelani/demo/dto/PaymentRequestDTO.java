package com.nelani.demo.dto;

import com.nelani.demo.model.PaymentProviderType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record PaymentRequestDTO(
                @NotBlank(message = "Order ID is required") String orderId,

                @NotNull(message = "Amount is required") @DecimalMin(value = "0.01", message = "Amount must be greater than zero") BigDecimal amount,

                @NotNull(message = "Provider is required") PaymentProviderType provider) {
}
