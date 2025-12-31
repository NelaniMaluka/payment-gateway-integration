package com.nelani.demo.service;

import com.nelani.demo.dto.PaymentRequestDTO;
import com.nelani.demo.dto.PaymentResponseDTO;

import java.util.List;

// Abstraction: interface hides implementation.
public interface PaymentService {

    List<PaymentResponseDTO> getAllPayments(int page, int size);

    PaymentResponseDTO initializePayment(PaymentRequestDTO request);

}
