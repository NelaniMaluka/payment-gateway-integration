package com.nelani.demo.service;

import com.nelani.demo.dto.PaymentRequestDTO;
import com.nelani.demo.dto.PaymentResponseDTO;
import com.nelani.demo.model.PaymentSortField;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

// Abstraction: interface hides implementation.
public interface PaymentService {

    Page<PaymentResponseDTO> getAllPayments(PaymentSortField field, Sort.Direction direction, int page, int size);

    PaymentResponseDTO initializePayment(PaymentRequestDTO request);

}
