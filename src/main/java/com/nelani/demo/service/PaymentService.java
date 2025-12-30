package com.nelani.demo.service;

import com.nelani.demo.dto.PaymentRequestDTO;
import com.nelani.demo.dto.PaymentResponseDTO;

public interface PaymentService {

    PaymentResponseDTO initializePayment(PaymentRequestDTO request);

}
