package com.nelani.demo.mapper;

import com.nelani.demo.dto.PaymentResponseDTO;
import com.nelani.demo.model.Payment;

public final class PaymentMapper {

    private PaymentMapper() {
    }

    public static PaymentResponseDTO toResponseDTO(Payment payment, String clientId, String secretId) {
        return new PaymentResponseDTO(
                payment.getOrderId(),
                clientId,
                secretId,
                payment.getAmount(),
                payment.getProvider(),
                payment.getStatus().name(),
                payment.getCreatedAt(),
                payment.getExpiresAt(),
                payment.getCompletedAt());
    }
}
